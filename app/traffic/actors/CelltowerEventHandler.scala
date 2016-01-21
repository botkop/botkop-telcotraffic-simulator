package traffic.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.client.ClusterClient.Publish
import akka.cluster.pubsub.DistributedPubSub
import play.libs.Akka
import traffic.model.Celltower
import traffic.protocol.CelltowerEvent

class CelltowerEventHandler(celltower: Celltower, template: CelltowerTemplate) extends Actor with ActorLogging {

    import CelltowerEventHandler._

    val mediator = DistributedPubSub(Akka.system()).mediator

    var counter = 0L

    def emitEvent(bearerId: UUID) = {

        counter = counter + 1

        var metrics = template.metrics.map { mt =>
            (mt.name, mt.dist.sample())
        }.toMap

        // add counter to the metrics map
        metrics += ("eventCounter" -> counter)

        val celltowerEvent = CelltowerEvent(celltower, bearerId.toString, metrics)

        celltowerEvent.publishTo(mediator)

        // mediator ! Publish("celltower-topic", celltowerEvent)
    }

    override def receive: Receive = {
        case EmitCelltowerEvent(bearerId) => emitEvent(bearerId)
    }

}

object CelltowerEventHandler {
    def props(celltower: Celltower, template: CelltowerTemplate) =
        Props(new CelltowerEventHandler(celltower, template))
    case class EmitCelltowerEvent(bearerId: UUID)
}

