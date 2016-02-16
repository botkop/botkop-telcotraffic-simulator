package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import breeze.stats.distributions.Gaussian
import traffic.model.{Celltower, Trip}
import traffic.protocol.{AttachEvent, CelltowerEvent}

class CelltowerEventHandler(celltower: Celltower, template: CelltowerTemplate) extends Actor with ActorLogging {

    import CelltowerEventHandler._

    var counter = 0L

    def emitEvent(trip: Trip) = {

        counter = counter + 1

        var metrics = template.metrics.map { mt =>
            if (mt.anomalyFreq != 0 && counter % mt.anomalyFreq == 0) {
                val anomalyDist = Gaussian(mt.dist.mean + mt.dist.sigma * 2.0, mt.dist.sigma)
                (mt.name, anomalyDist.sample())
            }
            else {
                (mt.name, mt.dist.sample())
            }
        }.toMap

        // add counter to the metrics map
        metrics += ("eventCounter" -> counter)

        val celltowerEvent = CelltowerEvent(celltower, trip.bearerId.toString, metrics)
        celltowerEvent.publish()

    }

    def emitAttachEvent(trip: Trip): Unit = {
        val cae = AttachEvent(trip.bearerId.toString, trip.subscriber)
        cae.publish()
    }

    override def receive: Receive = {
        case EmitCelltowerEvent(trip) =>
            log.debug("emitting event for {}", trip.bearerId)
            emitEvent(trip)
        case EmitAttachEvent(trip) =>
            emitAttachEvent(trip)
    }

}

object CelltowerEventHandler {
    def props(celltower: Celltower, template: CelltowerTemplate) =
        Props(new CelltowerEventHandler(celltower, template))
    case class EmitCelltowerEvent(trip: Trip)
    case class EmitAttachEvent(trip: Trip)
}

