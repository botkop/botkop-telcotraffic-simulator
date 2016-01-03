package actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import model.{Celltower, LocationEvent}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class CelltowerActor(celltower: Celltower,
                     frequency: FiniteDuration,
                     metricsBroker: ActorRef
                    ) extends Actor with ActorLogging {
    import CelltowerActor._

    override def receive: Receive = {
        case CelltowerGo =>
            val metrics = generateRandomLocationEvent()
            metricsBroker ! metrics
            context.system.scheduler.scheduleOnce(frequency, self, CelltowerGo)

        case CelltowerStop =>
            context.stop(self)
    }

    def generateRandomLocationEvent(): LocationEvent = {
        LocationEvent(celltower)
    }

}

object CelltowerActor {
    case object CelltowerGo
    case object CelltowerStop
}
