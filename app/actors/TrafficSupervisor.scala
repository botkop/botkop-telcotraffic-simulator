package actors

import actors.TripActor.{StopTrip, StartTrip, TripProgress}
import akka.actor._
import geo.LatLng
import model.{Celltower, Route, Subscriber, Trip}
import squants.motion.Velocity
import squants.time.Time

class TrafficSupervisor(mcc: Int, mnc: Int, numTrips: Int, slideSize: Time, velocity: Velocity, broker: ActorRef)
    extends Actor with ActorLogging {

    import TrafficSupervisor._

    def startTraffic() = {
        for (trip <- 1 to numTrips) {
            val trip = makeTrip()
            val tripActor = context.actorOf(TripActor.props(trip, slideSize, self))
            tripActor ! StartTrip
        }
    }

    def makeTrip(): Trip = {
        val fromTo = Celltower.getRandom(mcc, mnc, 2).map {
            _.location
        }
        val route = Route.byGoogle(fromTo.head, fromTo.last).get
        val sub = Subscriber.random().head
        Trip(sub, route, velocity)
    }

    def processProgress(progress: TripProgress) = {

        val location: LatLng = progress.actualLocation
        val nearestCell = Celltower.getNearest(mcc, mnc, location)

        val json: String =
            s"""
               |{
               |  "id": ${progress.trip.subscriber.id},
               |  "location": ${location.toJson},
               |  "nearestCell": ${nearestCell.toJson}
               |  }
               |}
             """.stripMargin

        broker ! json
    }

    def stopTraffic(): Unit = {
        context.children.foreach(_ ! StopTrip)
        self ! PoisonPill
    }

    override def receive = {
        case StartTraffic =>
            log.debug("starting")
            startTraffic()
        case StopTraffic =>
            log.debug("stopping")
            stopTraffic()
        case progress: TripProgress =>
            processProgress(progress)
    }

}

object TrafficSupervisor {

    case object StartTraffic
    case object StopTraffic

    def props(mcc: Int, mnc: Int, numTrips: Int, slideSize: Time, velocity: Velocity, broker: ActorRef): Props =
        Props(new TrafficSupervisor(mcc, mnc, numTrips, slideSize, velocity, broker))
}
