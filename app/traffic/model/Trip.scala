package traffic.model

import java.util.UUID

import geo.LatLng
import squants.Velocity
import squants.motion._
import squants.space.Meters

case class Trip (subscriber: Subscriber,
                 route: Route,
                 velocity: Velocity,
                 distanceCovered: Distance = Meters(0.0),
                 bearerId: UUID = UUID.randomUUID()) {

    def currentLocation: Option[LatLng] = {
        route.location(distanceCovered)
    }

    def totalDistance: Distance = Meters(route.distance)
}

object Trip {
    def random(mcc: Int, mnc: Int, velocity: Velocity): Trip = {
        val sub = Subscriber.random().head
        val fromTo = Celltower.getRandom(mcc, mnc, 2).map { _.location }
        val route = Route.byGoogle(fromTo.head, fromTo(1)).get

        Trip(sub, route, velocity)
    }

    def apply(trip: Trip, newDistanceCovered: Distance): Trip = {
        Trip(trip.subscriber, trip.route, trip.velocity, newDistanceCovered, trip.bearerId)
    }
}
