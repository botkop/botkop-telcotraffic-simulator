package traffic.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import traffic.helpers.JsonMessageParser

class SimulatorSocket(socket: ActorRef, simulator: ActorRef) extends Actor with ActorLogging {

    override def receive: Receive = {

        case message: String =>
            log.debug(s"received $message")

            val messageObject = JsonMessageParser.interpreteMessage(message)
            simulator ! messageObject
            socket ! message
    }
}

object SimulatorSocket {
    def props(socket: ActorRef, simulator: ActorRef) =
        Props(new SimulatorSocket(socket, simulator))
}

