package traffic.protocol

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import play.libs.Akka

abstract class TopicEvent(topic: String) {

    lazy val mediator = DistributedPubSub(Akka.system()).mediator

    def publish() = {
        mediator ! Publish(topic, this)
    }
}

