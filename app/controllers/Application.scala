package controllers

import java.util
import javax.inject.{Inject, Singleton}

import akka.actor.{PoisonPill, ActorSystem}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings, ClusterSingletonManager, ClusterSingletonManagerSettings}
import com.typesafe.scalalogging.LazyLogging
import play.api.Configuration
import play.api.Play.current
import play.api.libs.json.Json
import play.api.mvc._
import traffic.actors.TrafficSimulator.CurrentRequest
import traffic.actors.{SimulatorSocket, TrafficSimulator}
import traffic.brokers.{MessageBroker, MessageProvider}
import traffic.model.Celltower

import scala.collection.JavaConversions._

@Singleton
class Application @Inject() (val system: ActorSystem) extends Controller with LazyLogging {

    val mediator = DistributedPubSub(system).mediator

    initBrokers()

    // val trafficSimulator = system.actorOf(TrafficSimulator.props(), "traffic-simulator")

    system.actorOf(ClusterSingletonManager.props(
        singletonProps = TrafficSimulator.props(),
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system).withRole("simulate")),
        name = "TrafficSimulator")

    val trafficSimulatorProxy = system.actorOf(ClusterSingletonProxy.props(
        singletonManagerPath = "/user/TrafficSimulator",
        settings = ClusterSingletonProxySettings(system).withRole("simulate")),
        name = "TrafficSimulatorProxy")

    /**
      * initialize message brokers
      */
    def initBrokers() = {
        val conf = current.configuration

        val brokers = conf.getStringList("messageBrokers")
            .getOrElse(new util.ArrayList[String]())
            .map { brokerName =>

                val brokerConfig: Configuration = conf.getConfig(brokerName).get
                val clazzName = brokerConfig.getString("class").get

                val broker = Class.forName(clazzName).newInstance.asInstanceOf[MessageBroker]

                brokerConfig.getConfig("properties") match {
                    case Some(properties) =>
                        broker.configure(properties)
                    case _ =>
                }

                broker
            }.toList

        /*
        todo: this should not be a singleton, except for the websocketBroker
         */
        system.actorOf(ClusterSingletonManager.props(
            singletonProps = MessageProvider.props(brokers),
            terminationMessage = PoisonPill,
            settings = ClusterSingletonManagerSettings(system).withRole("publish")),
            name = "TrafficBroker")

        system.actorOf(ClusterSingletonProxy.props(
            singletonManagerPath = "/user/TrafficBroker",
            settings = ClusterSingletonProxySettings(system).withRole("publish")),
            name = "TrafficBrokerProxy")

        // system.actorOf(MessageProvider.props(brokers))
    }

    /**
      * handle request from REST interface
      * publish to mediator
      * @return Ok
      */
    def restRequest = Action(BodyParsers.parse.json) { request =>
        mediator ! Publish("request-topic", request.body)
        Ok(Json.obj("status" -> "OK"))
    }

    /**
      * REST request to obtain list of celltowers for mcc and mnc
      * Retrieve all celltowers and return as json to requester
      */
    def listCelltowers(mcc: Int, mnc: Int) = Action {
        val celltowers = Celltower.getAll(mcc, mnc)
        Ok(Json.toJson(celltowers))
    }

    /**
      * Handle HTTP request: show home page
      */
    def simulatorPage() = Action { implicit request =>
        Ok(views.html.simulator(request))
    }

    /**
      * Socket for HTTP clients
      */
    def simulatorSocket() = WebSocket.acceptWithActor[String, String] { req => socket =>
        logger.debug("establishing connection to websocket")

        // push current state to socket
        trafficSimulatorProxy ! CurrentRequest(socket)

        SimulatorSocket.props(socket)
    }

}
