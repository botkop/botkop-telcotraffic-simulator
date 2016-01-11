package traffic.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import traffic.model.JsonMessageParser

class SimulatorSocketHandler(socket: ActorRef, simulator: ActorRef) extends Actor with ActorLogging {

    val parser = JsonMessageParser(simulator)

    override def receive: Receive = {
        case message: String =>
            log.debug(s"received $message")
            parser.interpreteMessage(message)
    }
}

object SimulatorSocketHandler {
    def props(socket: ActorRef, simulator: ActorRef) = Props(new SimulatorSocketHandler(socket, simulator))
}
