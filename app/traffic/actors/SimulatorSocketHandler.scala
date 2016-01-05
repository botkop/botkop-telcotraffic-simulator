package traffic.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.libs.json.Json
import traffic.actors.TrafficSimulator.{StopSimulation, StartSimulation}
import traffic.model.SimulatorRequest

class SimulatorSocketHandler(socket: ActorRef, simulator: ActorRef) extends Actor with ActorLogging {

    override def receive: Receive = {
        case message: String =>
            log.debug(s"received $message")

            message match {
                case "stop" =>
                    simulator ! StopSimulation
                case message: String =>
                    Json.parse(message).asOpt[SimulatorRequest] match {
                        case Some(request) =>
                            simulator ! StartSimulation(request)
                        case None =>
                            // ignore
                    }
            }
    }
}

object SimulatorSocketHandler {
    def props(socket: ActorRef, simulator: ActorRef) = Props(new SimulatorSocketHandler(socket, simulator))
}
