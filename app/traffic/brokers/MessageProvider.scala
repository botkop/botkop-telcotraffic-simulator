package traffic.brokers

import akka.actor.{Props, Actor}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import play.api.libs.json.Json
import traffic.protocol.{CelltowerAttachEvent, TopicEvent, CelltowerEvent, SubscriberEvent}

class MessageProvider(brokers: List[MessageBroker]) extends Actor {

    val mediator = DistributedPubSub(context.system).mediator

    mediator ! Subscribe("subscriber-topic", self)
    mediator ! Subscribe("celltower-topic", self)
    mediator ! Subscribe("celltower-attach-topic", self)

    override def receive: Receive = {
        case event: SubscriberEvent =>
            val message = Json.stringify(Json.toJson(event))
            brokers.foreach(_.send(event.topic, message))
        case event: CelltowerEvent =>
            val message = Json.stringify(Json.toJson(event))
            brokers.foreach(_.send(event.topic, message))
        case event: CelltowerAttachEvent =>
            val message = Json.stringify(Json.toJson(event))
            brokers.foreach(_.send(event.topic, message))
    }
}

object MessageProvider {
    def props(brokers: List[MessageBroker]) = Props(new MessageProvider(brokers))
}

