package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import squants.motion.KilometersPerHour
import squants.time.Milliseconds
import traffic.actors.TripHandler.{SetSpeedFactor, StartTrip}
import traffic.brokers.MessageBroker
import traffic.model._

class TrafficSimulator(broker: MessageBroker) extends Actor with ActorLogging {

    import TrafficSimulator._

    def startSimulation(r: SimulatorRequest) = {

        // stop running simulation before starting a new one
        stopSimulation()

        val slide = Milliseconds(r.slide)
        val velocity = KilometersPerHour(r.velocity)

        val tripHandler = context.actorOf(TripHandler.props(r.mcc, r.mnc, slide, broker))

        log.info("starting simulation")
        for (i <- 1 to r.numTrips) {
            val trip = Trip.random(r.mcc, r.mnc, velocity)
            tripHandler ! StartTrip(trip)
        }
    }

    def stopSimulation() = {
        log.info("stopping simulation")
        context.children.foreach(context.stop)
    }

    def setSpeedFactor(ssf: SetSpeedFactor) = {
        context.children.foreach( _ ! ssf)
    }

    override def receive: Receive = {
        case StartSimulation(r) => startSimulation(r)
        case StopSimulation => stopSimulation()
        case ssf: SetSpeedFactor => setSpeedFactor(ssf)
    }
}

object TrafficSimulator {
    def props(broker: MessageBroker) = Props(new TrafficSimulator(broker))

    case class StartSimulation(request: SimulatorRequest)
    case object StopSimulation

}

