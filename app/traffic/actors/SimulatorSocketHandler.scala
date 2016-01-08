package traffic.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.libs.json.{JsValue, Json}
import traffic.actors.TrafficSimulator.{StartSimulation, StopSimulation}
import traffic.actors.TripHandler.SetSpeedFactor
import traffic.model.SimulatorRequest

class SimulatorSocketHandler(socket: ActorRef, simulator: ActorRef) extends Actor with ActorLogging {

    def setSpeedFactor(json: JsValue) = {
        val factor: Double = (json \ "speedFactor").as[Double]
        simulator ! SetSpeedFactor(factor)
    }

    def stopSimulator() = {
        simulator ! StopSimulation
    }

    def startSimulator(json: JsValue) = {
        (json \ "request").asOpt[SimulatorRequest] match {
            case Some(r) =>
                simulator ! StartSimulation(r)
        }
    }

    def interpreteMessage(message: String) = {
        val json: JsValue = Json.parse(message)

        val action: String = (json \ "action").as[String]

        action match {
            case "start" => startSimulator(json)
            case "setSpeedFactor" => setSpeedFactor(json)
            case "stop" => stopSimulator()
        }

    }

    override def receive: Receive = {
        case message: String =>
            log.debug(s"received $message")
            interpreteMessage(message)
    }
}

object SimulatorSocketHandler {
    def props(socket: ActorRef, simulator: ActorRef) = Props(new SimulatorSocketHandler(socket, simulator))
}
