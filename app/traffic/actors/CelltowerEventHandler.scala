package traffic.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import play.api.libs.json.Json
import traffic.brokers.MessageBroker
import traffic.model.Celltower

class CelltowerEventHandler(celltower: Celltower, template: CelltowerTemplate, broker: MessageBroker) extends Actor with ActorLogging {

    import CelltowerEventHandler._

    case class CelltowerEvent(celltower: Celltower, bearerId: String, metrics: Map[String, Double])
    object CelltowerEvent {
        implicit val w = Json.writes[CelltowerEvent]
    }

    def emitEvent(bearerId: UUID) = {
        val metrics = template.metrics.map { mt =>
            (mt.name, mt.dist.sample())
        }.toMap

        val celltowerEvent = CelltowerEvent(celltower, bearerId.toString, metrics)
        val message = Json.stringify(Json.toJson(celltowerEvent))

        broker.send("celltower-topic", message)
    }

    override def receive: Receive = {
        case EmitEvent(bearerId) => emitEvent(bearerId)
    }
}

object CelltowerEventHandler {
    def props(celltower: Celltower, template: CelltowerTemplate, broker: MessageBroker) =
        Props(new CelltowerEventHandler(celltower, template, broker))
    case class EmitEvent(bearerId: UUID)

}

