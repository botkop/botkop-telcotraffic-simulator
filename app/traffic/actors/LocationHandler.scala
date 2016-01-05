package traffic.actors

import akka.actor.{Actor, ActorRef, Props}
import traffic.model.Trip
import traffic.actors.CelltowerLocationHandler.HandleCelltowerLocation
import traffic.actors.SubscriberLocationHandler.HandleSubscriberLocation
import traffic.brokers.MessageBroker

class LocationHandler(mcc: Int, mnc: Int, broker: MessageBroker) extends Actor {

    import LocationHandler._

    val subscriberLocationHandler: ActorRef = context.actorOf(SubscriberLocationHandler.props(mcc, mnc, broker))
    val celltowerLocationHandler: ActorRef = context.actorOf(CelltowerLocationHandler.props(mcc, mnc, broker))

    override def receive: Receive = {

        case HandleLocation(trip: Trip) =>
            subscriberLocationHandler ! HandleSubscriberLocation(trip: Trip)
            celltowerLocationHandler ! HandleCelltowerLocation(trip: Trip)

    }
}

object LocationHandler {
    def props(mcc: Int, mnc: Int, broker: MessageBroker) =
        Props(new LocationHandler(mcc, mnc, broker))
    case class HandleLocation(trip: Trip)
}
