package controllers

import akka.actor.ActorSystem
import play.api.Play.current
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import traffic.actors.{SimulatorSocketHandler, TrafficSimulator}
import traffic.actors.TrafficSimulator.{StartSimulation, StopSimulation}
import traffic.brokers.{LogBroker, MessageBroker}
import traffic.model.SimulatorRequest

class Application extends Controller {

    val system = ActorSystem("TrafficSimulatorSystem")

    val broker: MessageBroker = new LogBroker()

    val trafficSimulator = system.actorOf(TrafficSimulator.props(broker))

    def startSimulator = Action(BodyParsers.parse.json) { request =>
        val r = request.body.validate[SimulatorRequest]
        r.fold(
            errors => {
                BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toJson(errors)))
            },
            simulatorRequest => {
                // start simulator
                trafficSimulator ! StartSimulation(simulatorRequest)

                Ok(Json.obj("status" -> "OK"))
            }
        )
    }

    def stopSimulator = Action { request =>
        // stop simulator
        trafficSimulator ! StopSimulation

        Ok(Json.obj("status" -> "OK"))
    }


    def simulatorPage() = Action { implicit request =>
        Ok(views.html.simulator(request))
    }

    def simulatorSocket() = WebSocket.acceptWithActor[String, String] { req => out =>
        SimulatorSocketHandler.props(out, trafficSimulator)
    }


}
