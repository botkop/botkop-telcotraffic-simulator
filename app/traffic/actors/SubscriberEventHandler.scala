package traffic.actors

import akka.actor.{Actor, Props}
import traffic.model.Trip
import traffic.protocol.SubscriberEvent

class SubscriberEventHandler() extends Actor {
    import SubscriberEventHandler._

    override def receive: Receive = {
        case HandleSubscriberEvent(trip) =>
            handleMessage(trip)
    }

    def handleMessage(trip: Trip) = {
        val subscriberEvent = SubscriberEvent.extract(trip)
        subscriberEvent.publish()
    }

}

object SubscriberEventHandler {
    def props() = Props(new SubscriberEventHandler())
    case class HandleSubscriberEvent(trip: Trip)
}
