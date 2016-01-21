package traffic.actors

import akka.actor.{Actor, ActorRef, Props}
import traffic.actors.CelltowerLocationHandler.HandleCelltowerLocation
import traffic.actors.SubscriberEventHandler.HandleSubscriberEvent
import traffic.model.Trip

class LocationHandler(mcc: Int, mnc: Int) extends Actor {

    import LocationHandler._

    val subscriberLocationHandler: ActorRef = context.actorOf(SubscriberEventHandler.props())
    val celltowerLocationHandler: ActorRef = context.actorOf(CelltowerLocationHandler.props(mcc, mnc))

    override def receive: Receive = {

        case HandleLocation(trip: Trip) =>
            subscriberLocationHandler ! HandleSubscriberEvent(trip: Trip)
            celltowerLocationHandler ! HandleCelltowerLocation(trip: Trip)

    }

}

object LocationHandler {
    def props(mcc: Int, mnc: Int) = Props(new LocationHandler(mcc, mnc))
    case class HandleLocation(trip: Trip)
}
