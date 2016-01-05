package traffic.actors

import akka.actor.{Actor, Props}
import geo.LatLng
import traffic.model.{Trip, SubscriberLocation}
import play.api.libs.json.Json
import traffic.brokers.MessageBroker

class SubscriberLocationHandler(mcc: Int, mnc: Int, broker: MessageBroker) extends Actor {
    import SubscriberLocationHandler._

    override def receive: Receive = {
        case HandleSubscriberLocation(subscriberLocation) =>
            handleMessage(subscriberLocation)
    }

    def handleMessage(trip: Trip) = {
        val subscriberLocation = SubscriberLocation.extract(trip)
        val message = Json.stringify(Json.toJson(subscriberLocation))
        broker.send(message)
    }

}

object SubscriberLocationHandler {
    def props(mcc: Int, mnc: Int, broker: MessageBroker) = Props(new SubscriberLocationHandler(mcc, mnc, broker))

    case class HandleSubscriberLocation(trip: Trip)
}
