package traffic.protocol

import traffic.brokers.ConfiguredBrokers

abstract class TopicEvent(topic: String) extends Serializable {

    /*
    lazy val mediator = DistributedPubSub(Akka.system()).mediator

    def publish() = {
        mediator ! Publish(topic, this)
    }
    */

    def publish() = {
        ConfiguredBrokers.publish(this)
    }

}

