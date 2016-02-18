package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import squants._
import squants.motion.KilometersPerHour
import squants.time.Milliseconds
import traffic.actors.LocationHandler.HandleLocation
import traffic.model.Trip
import traffic.protocol.{RequestEvent, RequestUpdateEvent}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

class TripHandler() extends Actor with ActorLogging {

    import TripHandler._

    val locationHandler = context.actorOf(LocationHandler.props())

    var isStopped: Boolean = false

    /*
    must initialize below variables, because a 'stand-by' TripHandler actor may have been created in the pool
    and update requests (via Broadcast) can arrive before start requests
    alternatively, an update request could be broadcasted just after creation of the pool, and before start requests
     */
    case class TripFactors(speed: Velocity = KilometersPerHour(0), slide: Time = Milliseconds(0)) {
        val slideDuration = Duration(slide.millis, MILLISECONDS)
        val slideDistance = speed * slide
    }

    var tripFactors = TripFactors()

    def continueTrip(trip: Trip): Unit =  {
        if (trip.distanceCovered >= trip.totalDistance) {
            val nextTrip: Trip = Trip(trip, trip.totalDistance)
            locationHandler ! HandleLocation(nextTrip)
            log.info("trip {}: finished", trip.bearerId)

            // start a new trip
            val newTrip = Trip.random(trip.mcc, trip.mnc, trip.velocity, trip.slide)
            log.info("trip {}: starting", newTrip.bearerId)
            continueTrip(newTrip)
        }
        else {
            val nextTrip = Trip(trip, trip.distanceCovered + tripFactors.slideDistance)
            locationHandler ! HandleLocation(nextTrip)
            context.system.scheduler.scheduleOnce(tripFactors.slideDuration, self, ContinueTrip(nextTrip))
        }
    }

    def startTrip(request: RequestEvent): Unit = {
        val trip = Trip.random(request.mcc, request.mnc, KilometersPerHour(request.velocity), Milliseconds(request.slide))
        startTrip(trip)
    }

    def startTrip(trip: Trip): Unit = {
        isStopped = false
        log.info("trip {}: starting", trip.bearerId)
        tripFactors = TripFactors(trip.velocity, trip.slide)
        continueTrip(trip)
    }

    def requestUpdate(update: RequestUpdateEvent) = {
        update.slide match {
            case Some(d) =>
                tripFactors = TripFactors(tripFactors.speed, Milliseconds(d))
            case None =>
        }

        update.velocity match {
            case Some(d) =>
                tripFactors = TripFactors(KilometersPerHour(d), tripFactors.slide)
            case None =>
        }

    }

    override def receive = {

        case StartTrip(request) =>
            startTrip(request)

        case ContinueTrip(trip) if ! isStopped =>
            continueTrip(trip)

        case update: RequestUpdateEvent =>
            requestUpdate(update)

        case StopTrip =>
            isStopped = true
            // context.stop(self) // cannot use this, because it would kill the pool

        case trip: Trip => // used for unit testing only
            startTrip(trip)
    }
}

object TripHandler {

    case class StartTrip(request: RequestEvent) extends ConsistentHashable {
        // todo: a random is probably not the best hashing key for the pool
        // or maybe we should not use a hashing pool in the first place
        override def consistentHashKey: Any = Random.nextInt
    }

    case class ContinueTrip(trip: Trip)
    case object StopTrip

    def props (): Props = {
        Props(new TripHandler())
    }
}

