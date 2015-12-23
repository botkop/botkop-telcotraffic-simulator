package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.libs.json.{JsValue, Json}
import squants.motion.KilometersPerHour
import squants.time.Milliseconds

class SimulatorSocketHandler(socket: ActorRef) extends Actor with ActorLogging {

    import actors.TrafficSupervisor._

    var trafficSupervisor: ActorRef = _

    def parse(message: String) = {
        val json: JsValue = Json.parse(message)
        val action = (json \ "action").as[String]

        action match {
            case "start" => startSimulation(json)
            case "stop" => stopSimulation()
        }
    }

    def startSimulation(json: JsValue) = {
        val mcc = (json \ "mcc").as[Int]
        val mnc = (json \ "mnc").as[Int]
        val numTrips = (json \ "numTrips").as[Int]
        val slideSize = Milliseconds((json \ "slideSize").as[Int])
        val velocity = KilometersPerHour((json \ "velocity").as[Int])

        stopSimulation()

        trafficSupervisor = context.actorOf(TrafficSupervisor.props(mcc, mnc, numTrips, slideSize, velocity, socket))

        trafficSupervisor ! StartTraffic
    }

    def stopSimulation() = {
        if (trafficSupervisor != null) {
            trafficSupervisor ! StopTraffic
        }
    }

    override def receive: Receive = {
        case message: String =>
            log.debug(s"received $message")
            parse(message)
        case _ =>
            log.debug("received garbage")
    }
}

object SimulatorSocketHandler {
    def props(out: ActorRef) = Props(new SimulatorSocketHandler(out))
}
