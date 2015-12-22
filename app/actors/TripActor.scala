package actors

import akka.actor.{ActorRef, Actor, PoisonPill, Props}
import com.typesafe.scalalogging.LazyLogging
import model.Trip
import squants.space.Meters
import squants.{Length, Time}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class TripActor(trip: Trip, slideSize: Time, broker: ActorRef) extends Actor with LazyLogging {
    import TripActor._

    val routeDistance: Length = Meters(trip.route.distance)
    val publishDuration: FiniteDuration = Duration(slideSize.millis, MILLISECONDS)
    val slideDistance: Length = trip.velocity * slideSize

    def startTrip(): Unit = {
        continueTrip()
    }

    def endTrip(): Unit = {
        logger.debug(s"[$trip] route end reached: $routeDistance")
        self ! PoisonPill
    }

    def continueTrip(currentDistance: Length = Meters(0.0)): Unit =  {
        if (currentDistance >= routeDistance) {
            broker ! TripProgress(trip, routeDistance)
            logger.debug(s"[$trip] distance covered: $routeDistance")
            endTrip()
        }
        else {
            broker ! TripProgress(trip, currentDistance)
            logger.debug(s"[$trip] distance covered: $currentDistance")
            val newDistance = currentDistance + slideDistance
            context.system.scheduler.scheduleOnce(publishDuration, self, newDistance)
        }
    }

    override def receive =  {
        case StartTrip => startTrip()
        case currentDistance: Length => continueTrip(currentDistance)
    }
}

object TripActor {

    case object StartTrip

    case class TripProgress(trip: Trip, distance: Length)

    def props(trip: Trip, slideSize: Time, broker: ActorRef): Props = Props(new TripActor(trip, slideSize, broker))
}
