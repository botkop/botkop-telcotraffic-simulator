package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import squants._
import squants.time.Time
import traffic.actors.LocationHandler.HandleLocation
import traffic.model.Trip

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class TripHandler(mcc: Int, mnc: Int, slide: Time) extends Actor with ActorLogging {

    import TripHandler._

    val locationHandler = context.actorOf(LocationHandler.props(mcc, mnc))

    val slideDuration: FiniteDuration = Duration(slide.millis, MILLISECONDS)

    def continueTrip(trip: Trip): Unit =  {
        if (trip.distanceCovered >= trip.totalDistance) {
            val nextTrip: Trip = Trip(trip, trip.totalDistance)
            locationHandler ! HandleLocation(nextTrip)
            log.info("trip {}: finished", trip.bearerId)

            context.stop(self)

        }
        else {
            val slideDistance: Length = trip.velocity * slide
            val nextTrip = Trip(trip, trip.distanceCovered + slideDistance)
            locationHandler ! HandleLocation(nextTrip)
            context.system.scheduler.scheduleOnce(slideDuration, self, ContinueTrip(nextTrip))
        }
    }

    def startTrip(trip: Trip): Unit = {
        log.info("trip {}: starting", trip.bearerId)
        continueTrip(trip)
    }

    override def receive = {
        case StartTrip(trip) =>
            startTrip(trip)
        case ContinueTrip(trip) =>
            continueTrip(trip)
    }
}

object TripHandler {

    case class StartTrip(trip: Trip)
    case class ContinueTrip(trip: Trip)

    def props (mcc: Int, mnc: Int, slide: Time): Props = {
        Props(new TripHandler(mcc, mnc, slide))
    }
}

