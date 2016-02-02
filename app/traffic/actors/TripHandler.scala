package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import squants._
import traffic.actors.LocationHandler.HandleLocation
import traffic.model.Trip
import traffic.protocol.RequestUpdateEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class TripHandler() extends Actor with ActorLogging {

    import TripHandler._

    val locationHandler = context.actorOf(LocationHandler.props())

    var slideFactor: Double = 1.0
    var speedFactor: Double = 1.0

    def continueTrip(trip: Trip): Unit =  {
        if (trip.distanceCovered >= trip.totalDistance) {
            val nextTrip: Trip = Trip(trip, trip.totalDistance)
            locationHandler ! HandleLocation(nextTrip)
            log.info("trip {}: finished", trip.bearerId)

            context.stop(self)
        }
        else {
            val slide = trip.slide * slideFactor
            val slideDuration = Duration(slide.millis, MILLISECONDS)
            val slideDistance: Length = (trip.velocity * speedFactor) * slide
            val nextTrip = Trip(trip, trip.distanceCovered + slideDistance)
            locationHandler ! HandleLocation(nextTrip)

            context.system.scheduler.scheduleOnce(slideDuration, self, ContinueTrip(nextTrip))
        }
    }

    def startTrip(trip: Trip): Unit = {
        log.info("trip {}: starting", trip.bearerId)
        continueTrip(trip)
    }

    def requestUpdate(update: RequestUpdateEvent) = {
        update.slide match {
            case Some(d) =>
                // TODO: fix hardcoding
                slideFactor = d / 500.0
            case None =>
        }

        update.velocity match {
            case Some(d) =>
                // TODO: this is a temporary solution until we implement variable speed per trip
                speedFactor = d / 120.0
            case None =>
        }
    }

    override def receive = {
        case StartTrip(trip) =>
            startTrip(trip)
        case ContinueTrip(trip) =>
            continueTrip(trip)
        case update: RequestUpdateEvent =>
            requestUpdate(update)
        case StopTrip =>
            context.stop(self)
    }
}

object TripHandler {

    case class StartTrip(trip: Trip) extends ConsistentHashable {
        override def consistentHashKey: Any = trip.bearerId
    }

    case class ContinueTrip(trip: Trip)
    case object StopTrip

    def props (): Props = {
        Props(new TripHandler())
    }
}

