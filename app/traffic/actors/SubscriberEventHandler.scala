package traffic.actors

import akka.actor.{Actor, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import play.libs.Akka
import traffic.model.Trip
import traffic.protocol.SubscriberEvent

class SubscriberEventHandler() extends Actor {
    import SubscriberEventHandler._

    val mediator = DistributedPubSub(Akka.system()).mediator

    override def receive: Receive = {
        case HandleSubscriberEvent(trip) =>
            handleMessage(trip)
    }

    def handleMessage(trip: Trip) = {
        val subscriberEvent = SubscriberEvent.extract(trip)
        // mediator ! Publish("subscriber-topic", subscriberEvent)
        subscriberEvent.publishTo(mediator)
    }

}

object SubscriberEventHandler {
    def props() = Props(new SubscriberEventHandler())
    case class HandleSubscriberEvent(trip: Trip)
}
