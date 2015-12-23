package actors

import akka.actor._
import geo.LatLng
import model.Trip
import squants.space.Meters
import squants.{Length, Time}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class TripActor(trip: Trip, slideSize: Time, broker: ActorRef) extends Actor with ActorLogging {
    import TripActor._

    val routeDistance: Length = Meters(trip.route.distance)
    val publishDuration = Duration(slideSize.millis, MILLISECONDS)
    val slideDistance: Length = trip.velocity * slideSize

    def startTrip(): Unit = {
        continueTrip()
    }

    def endTrip(): Unit = {
        log.debug(s"[${trip.subscriber.id}] route end reached: $routeDistance")
        self ! PoisonPill
    }

    def continueTrip(currentDistance: Length = Meters(0.0)): Unit =  {
        if (currentDistance >= routeDistance) {
            broker ! TripProgress(trip, routeDistance)
            endTrip()
        }
        else {
            broker ! TripProgress(trip, currentDistance)
            val newDistance = currentDistance + slideDistance
            context.system.scheduler.scheduleOnce(publishDuration, self, newDistance)
        }
    }

    override def receive =  {
        case StartTrip =>
            log.debug("starting")
            startTrip()
        case StopTrip =>
            log.debug("stopping")
            endTrip()
        case currentDistance: Length => continueTrip(currentDistance)
    }
}

object TripActor {

    case object StartTrip
    case object StopTrip

    case class TripProgress(trip: Trip, distance: Length) {
        def actualLocation: LatLng = trip.route.position(distance.toMeters)
    }

    def props(trip: Trip, slideSize: Time, broker: ActorRef): Props = Props(new TripActor(trip, slideSize, broker))
}
