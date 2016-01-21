package traffic.brokers

import akka.actor.{Props, Actor}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import play.api.libs.json.Json
import traffic.protocol.{CelltowerEvent, SubscriberEvent}

class MessageProvider(broker: MessageBroker) extends Actor {

    val mediator = DistributedPubSub(context.system).mediator

    mediator ! Subscribe("subscriber-topic", self)
    mediator ! Subscribe("celltower-topic", self)

    override def receive: Receive = {
        case event: SubscriberEvent =>
            broker.send(event.topic, Json.stringify(Json.toJson(event)))
        case event: CelltowerEvent =>
            broker.send(event.topic, Json.stringify(Json.toJson(event)))
    }

}

object MessageProvider {
    def props(broker: MessageBroker) = Props(new MessageProvider(broker))
}


