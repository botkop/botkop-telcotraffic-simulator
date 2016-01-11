package traffic.model

import akka.actor.ActorRef
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json._
import traffic.actors.TrafficSimulator.{StartSimulation, StopSimulation}
import traffic.actors.TripHandler.SetSpeedFactor

case class JsonMessageParser (simulator: ActorRef) extends LazyLogging {

    def setSpeedFactor(json: JsValue) = {
        val factor: Double = (json \ "request" \ "speedFactor").as[Double]
        simulator ! SetSpeedFactor(factor)
    }

    def stopSimulator() = {
        simulator ! StopSimulation
    }

    def startSimulator(json: JsValue) = {
        val request = (json \ "request").as[SimulatorRequest]
        simulator ! StartSimulation(request)
    }

    def interpreteJson(json: JsValue) = {
        val action = (json \ "action").as[String]
        action match {
            case "start" => startSimulator(json)
            case "setSpeedFactor" => setSpeedFactor(json)
            case "stop" => stopSimulator()
        }
    }

    def interpreteMessage(message: String) = {
        val json: JsValue = Json.parse(message)
        interpreteJson(json)
    }
}
