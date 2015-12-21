package actors

import akka.actor.{Props, ActorLogging, Actor, ActorRef}

class WebSocketHandler(out: ActorRef) extends Actor with ActorLogging {
    override def receive: Receive = {
        case s: String =>
            log.info("received " + s)
            out ! s
        case _ =>
            log.info("received garbage")
    }
}

object WebSocketHandler {
    def props(out: ActorRef) = Props(new WebSocketHandler(out))
}
