package traffic.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import traffic.helpers.JsonMessageParser

class SimulatorSocketHandler(socket: ActorRef, simulator: ActorRef, messageInterpreter: JsonMessageParser) extends Actor with ActorLogging {

    override def receive: Receive = {
        case message: String =>
            log.debug(s"received $message")
            messageInterpreter.interpreteMessage(message)
    }
}

object SimulatorSocketHandler {
    def props(socket: ActorRef, simulator: ActorRef, messageInterpreter: JsonMessageParser) =
        Props(new SimulatorSocketHandler(socket, simulator, messageInterpreter))
}
