package traffic.protocol

import java.util.UUID

import botkop.geo.LatLng
import play.api.libs.json.Json
import traffic.model.{Subscriber, Trip}

case class SubscriberEvent(subscriber: Subscriber, location: LatLng, bearerId: UUID, topic: String = "subscriber-topic")
    extends TopicEvent(topic)

object SubscriberEvent {

    implicit val f = Json.format[SubscriberEvent]

    def extract(trip: Trip): SubscriberEvent = {
        SubscriberEvent(trip.subscriber, trip.currentLocation.get, trip.bearerId)
    }

}
