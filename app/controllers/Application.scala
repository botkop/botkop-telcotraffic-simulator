package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, PoisonPill}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.typesafe.scalalogging.LazyLogging
import play.api.Play.current
import play.api.libs.json.Json
import play.api.mvc._
import traffic.actors.TrafficSimulator.CurrentRequest
import traffic.actors.{SimulatorSocket, TrafficSimulator}
import traffic.model.Celltower

@Singleton
class Application @Inject() (val system: ActorSystem) extends Controller with LazyLogging {

    val mediator = DistributedPubSub(system).mediator

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
      * WebSocket for HTTP clients
      */
    def simulatorSocket() = WebSocket.acceptWithActor[String, String] { req => socket =>
        logger.debug("establishing connection to websocket")

        // push current state to socket
        trafficSimulatorProxy ! CurrentRequest(socket)

        SimulatorSocket.props(socket)
    }

}
