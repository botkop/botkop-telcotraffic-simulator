package traffic.protocol

import traffic.brokers.ConfiguredBrokers

abstract class TopicEvent(topic: String) extends Serializable {
    def publish() = {
        ConfiguredBrokers.publish(this)
    }
}

