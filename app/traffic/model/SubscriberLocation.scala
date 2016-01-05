package traffic.model

import java.util.UUID

import geo.LatLng
import play.api.libs.json.Json

case class SubscriberLocation(subscriber: Subscriber, location: LatLng, bearerId: UUID)

object SubscriberLocation {

    def extract(trip: Trip): SubscriberLocation = {
        SubscriberLocation(trip.subscriber, trip.currentLocation.get, trip.bearerId)
    }

    implicit val requestWrites = Json.writes[SubscriberLocation]
    implicit val requestReads = Json.reads[SubscriberLocation]

}
