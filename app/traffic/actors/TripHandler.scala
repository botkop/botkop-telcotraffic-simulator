package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import squants._
import squants.time.{Milliseconds, Time}
import traffic.actors.LocationHandler.HandleLocation
import traffic.model.Trip
import traffic.protocol.RequestUpdateEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class TripHandler(mcc: Int, mnc: Int, var slide: Time) extends Actor with ActorLogging {

    import TripHandler._

    val locationHandler = context.actorOf(LocationHandler.props(mcc, mnc))

    var slideDuration: FiniteDuration = Duration(slide.millis, MILLISECONDS)

    var speedFactor: Double = 1.0

    def continueTrip(trip: Trip): Unit =  {
        if (trip.distanceCovered >= trip.totalDistance) {
            val nextTrip: Trip = Trip(trip, trip.totalDistance)
            locationHandler ! HandleLocation(nextTrip)
            log.info("trip {}: finished", trip.bearerId)

            context.stop(self)
        }
        else {
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
                slide = Milliseconds(d)
                slideDuration = Duration(slide.millis, MILLISECONDS)
            case None =>
        }

        update.velocity match {
            case Some(d) =>
                // note: this is a temporary solution until we implement variable speed per trip
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
    }
}

object TripHandler {

    case class StartTrip(trip: Trip)
    case class ContinueTrip(trip: Trip)

    def props (mcc: Int, mnc: Int, slide: Time): Props = {
        Props(new TripHandler(mcc, mnc, slide))
    }
}

