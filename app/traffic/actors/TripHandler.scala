package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import squants._
import squants.time.Time
import traffic.actors.LocationHandler.HandleLocation
import traffic.brokers.MessageBroker
import traffic.model.Trip

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class TripHandler(mcc: Int, mnc: Int, slide: Time, broker: MessageBroker) extends Actor with ActorLogging {

    import TripHandler._

    val locationHandler = context.actorOf(LocationHandler.props(mcc, mnc, broker))

    val slideDuration: FiniteDuration = Duration(slide.millis, MILLISECONDS)

    var speedFactor: Double = 1.0

    def continueTrip(trip: Trip): Unit =  {
        if (trip.distanceCovered >= trip.totalDistance) {
            val nextTrip: Trip = Trip(trip, trip.totalDistance)
            locationHandler ! HandleLocation(nextTrip)
            log.info("trip {}: finished", trip.bearerId)
        }
        else {
            // log.debug("speedfactor is now: {}", speedFactor)
            val slideDistance: Length = (trip.velocity * speedFactor) * slide
            val nextTrip = Trip(trip, trip.distanceCovered + slideDistance)
            locationHandler ! HandleLocation(nextTrip)
            context.system.scheduler.scheduleOnce(slideDuration, self, ContinueTrip(nextTrip))
        }
    }

    def setSpeedFactor(factor: Double) = {
        log.info("setting speed factor to {}", factor)
        this.speedFactor = factor
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
        case SetSpeedFactor(factor) =>
            setSpeedFactor(factor)
    }
}

object TripHandler {

    case class StartTrip(trip: Trip)
    case class ContinueTrip(trip: Trip)
    case class SetSpeedFactor(factor: Double)

    def props (mcc: Int, mnc: Int, slide: Time, broker: MessageBroker): Props = {
        Props(new TripHandler(mcc, mnc, slide, broker))
    }
}

