package model

import squants.Velocity

case class Trip (subscriber: Subscriber, route: Route, velocity: Velocity)

object Trip {
    def random(mcc: Int, mnc: Int, velocity: Velocity): Trip = {
        val sub = Subscriber.random().head
        val fromTo = Celltower.getRandom(mcc, mnc, 2).map { _.location }
        val route = Route.byGoogle(fromTo.head, fromTo(1)).get

        Trip(sub, route, velocity)
    }
}
