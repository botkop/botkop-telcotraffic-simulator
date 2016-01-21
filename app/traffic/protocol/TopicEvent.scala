package traffic.protocol

import akka.actor.ActorRef
import akka.cluster.pubsub.DistributedPubSubMediator.Publish

abstract class TopicEvent(topic: String) {
    def publishTo(mediator: ActorRef) = {
        mediator ! Publish(topic, this)
    }
}


