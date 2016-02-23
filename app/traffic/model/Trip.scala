package traffic.model

import java.util.UUID

import botkop.geo.LatLng
import squants.Velocity
import squants.motion._
import squants.space.Meters
import squants.time.Time

case class Trip (mcc: Int,
                 mnc: Int,
                 subscriber: Subscriber,
                 route: Route,
                 velocity: Velocity,
                 slide: Time,
                 distanceCovered: Distance = Meters(0.0),
                 bearerId: UUID = UUID.randomUUID()) {

    def currentLocation: Option[LatLng] = {
        route.location(distanceCovered)
    }

    def totalDistance: Distance = Meters(route.distance)
}

object Trip {
    def random(mcc: Int, mnc: Int, velocity: Velocity, slide: Time): Trip = {
        val sub = Subscriber.random().head
        val fromTo = Celltower.getRandom(mcc, mnc, 2).map { _.location }
        val route = Route.byGoogle(fromTo.head, fromTo(1)).get

        Trip(mcc, mnc, sub, route, velocity, slide)
    }

    def apply(trip: Trip, newDistanceCovered: Distance): Trip = {
        Trip(trip.mcc, trip.mnc, trip.subscriber, trip.route, trip.velocity, trip.slide, newDistanceCovered, trip.bearerId)
    }
}
