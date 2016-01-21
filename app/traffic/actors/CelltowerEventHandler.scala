package traffic.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import play.api.libs.json.Json
import traffic.brokers.MessageBroker
import traffic.model.{Celltower, CelltowerEvent}

class CelltowerEventHandler(celltower: Celltower, template: CelltowerTemplate, broker: MessageBroker) extends Actor with ActorLogging {

    import CelltowerEventHandler._

    var counter = 0L

    def emitEvent(bearerId: UUID) = {

        counter = counter + 1

        var metrics = template.metrics.map { mt =>
            (mt.name, mt.dist.sample())
        }.toMap

        // add counter to the metrics map
        metrics += ("eventCounter" -> counter)

        val celltowerEvent = CelltowerEvent(celltower, bearerId.toString, metrics)
        val message = Json.stringify(Json.toJson(celltowerEvent))

        broker.send("celltower-topic", message)
    }

    override def receive: Receive = {
        case EmitCelltowerEvent(bearerId) => emitEvent(bearerId)
    }

}

object CelltowerEventHandler {
    def props(celltower: Celltower, template: CelltowerTemplate, broker: MessageBroker) =
        Props(new CelltowerEventHandler(celltower, template, broker))
    case class EmitCelltowerEvent(bearerId: UUID)
}

