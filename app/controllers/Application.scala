package controllers

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import play.api.Configuration
import play.api.Play.current
import play.api.libs.json.{JsError, JsResultException, Json}
import play.api.mvc._
import traffic.actors.{SimulatorSocketHandler, TrafficSimulator}
import traffic.brokers.MessageBroker
import traffic.model.JsonMessageParser

class Application extends Controller with LazyLogging {

    val system = ActorSystem("TrafficSimulatorSystem")

    val trafficSimulator = system.actorOf(TrafficSimulator.props())

    val messageInterpreter = JsonMessageParser(trafficSimulator)

    /**
      * handle REST requests
      * @return
      */
    def restRequest = Action(BodyParsers.parse.json) { request =>
        try {
            messageInterpreter.interpreteJson(request.body)
            Ok(Json.obj("status" -> "OK"))
        } catch {
            case jre: JsResultException =>
                BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toJson(jre.errors)))
            case e: Exception =>
                BadRequest(Json.obj("status" -> "OK", "message" -> e.getMessage))
        }
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
        SimulatorSocketHandler.props(out, trafficSimulator)
    }

}
