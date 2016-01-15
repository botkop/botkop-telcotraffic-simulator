package traffic.helpers

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json._
import traffic.actors.TrafficSimulator.{StartSimulation, StopSimulation}
import traffic.actors.TripHandler.SetSpeedFactor
import traffic.model.SimulatorRequest

case object JsonMessageParser extends LazyLogging {

    def setSpeedFactor(json: JsValue) = {
        val factor: Double = (json \ "request" \ "speedFactor").as[Double]
        SetSpeedFactor(factor)
    }

    def stopSimulator() = {
        StopSimulation
    }

    def startSimulator(json: JsValue) = {
        val request = (json \ "request").as[SimulatorRequest]
        StartSimulation(request)
    }

    def interpreteJson(json: JsValue): Product with Serializable = {
        val action = (json \ "action").as[String]
        action match {
            case "start" => startSimulator(json)
            case "setSpeedFactor" => setSpeedFactor(json)
            case "stop" => stopSimulator()
        }
    }

    def interpreteMessage(message: String): Product with Serializable = {
        val json: JsValue = Json.parse(message)
        interpreteJson(json)
    }
}

