package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.scalalogging.LazyLogging
import play.api.Configuration
import play.api.Play.current
import play.api.libs.json.Json
import play.api.mvc._
import traffic.actors.{SimulatorSocket, TrafficSimulator}
import traffic.brokers.MessageBroker

@Singleton
class Application @Inject() (val system: ActorSystem) extends Controller with LazyLogging {

    val mediator = DistributedPubSub(system).mediator

    val broker = initBroker

    val trafficSimulator = system.actorOf(TrafficSimulator.props(), "traffic-simulator")

    /**
      * initialize message broker
      * @return
      */
    def initBroker: MessageBroker = {
        val conf = current.configuration
        val brokerName: String = conf.getString("messageBroker").get
        val brokerConfig: Configuration = conf.getConfig(brokerName).get
        val clazzName = brokerConfig.getString("class").get

        val broker = Class.forName(clazzName).newInstance.asInstanceOf[MessageBroker]

        brokerConfig.getConfig("properties") match {
            case Some(properties) =>
                broker.configure(properties)
            case _ =>
        }
        broker
    }

    /**
      * handle REST requests
      * @return
      */
    def restRequest = Action(BodyParsers.parse.json) { request =>
        /*
        received from REST interface
        publish to mediator
        */
        mediator ! Publish("request-topic", request.body)
        Ok(Json.obj("status" -> "OK"))
    }

    /**
      * Handle HTTP requests
      * @return
      */
    def simulatorPage() = Action { implicit request =>
        Ok(views.html.simulator(request))
    }

    /**
      * Socket for HTTP clients
      * @return
      */
    def simulatorSocket() = WebSocket.acceptWithActor[String, String] { req => out =>
        SimulatorSocket.props(out)
    }

}
