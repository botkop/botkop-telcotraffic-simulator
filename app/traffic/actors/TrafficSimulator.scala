package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import squants.motion.{KilometersPerHour, Velocity}
import squants.time.{Milliseconds, Time}
import traffic.actors.TripHandler.ContinueTrip
import traffic.brokers.MessageBroker
import traffic.model._

class TrafficSimulator(broker: MessageBroker) extends Actor with ActorLogging {

    import TrafficSimulator._

    def makeTrip(mcc: Int, mnc: Int, velocity: Velocity): Trip = {
        val fromTo = Celltower.getRandom(mcc, mnc, 2).map {
            _.location
        }
        val route = Route.byGoogle(fromTo.head, fromTo.last).get
        val sub = Subscriber.random().head
        Trip(sub, route, velocity)
    }

    def startSimulation(mcc: Int, mnc: Int, numTrips: Int, slide: Time, velocity: Velocity) = {
        for (i <- 1 to numTrips) {
            val trip = makeTrip(mcc, mnc, velocity)
            val handler = context.actorOf(TripHandler.props(mcc, mnc, slide, broker))
            handler ! ContinueTrip(trip)
        }
    }

    def stopSimulation() = {
        log.info("stopping all children")
        context.children.foreach{context.stop}
    }

    override def receive: Receive = {
        case StartSimulation(r) =>
            log.info("starting simulation")
            startSimulation(r.mcc, r.mnc, r.numTrips, Milliseconds(r.slide), KilometersPerHour(r.velocity))
        case StopSimulation =>
            stopSimulation()
    }
}

object TrafficSimulator {
    def props(broker: MessageBroker) = Props(new TrafficSimulator(broker))

    case class StartSimulation(request: SimulatorRequest)
    case object StopSimulation

}

