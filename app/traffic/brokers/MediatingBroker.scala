package traffic.brokers

import javax.inject.Singleton

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.scalalogging.LazyLogging
import play.libs.Akka

@Singleton
class MediatingBroker extends MessageBroker with LazyLogging {

    val mediator = DistributedPubSub(Akka.system()).mediator

    override def send(topic: String, message: String): Unit = {
        val event =
            s"""{
               |  "topic": "$topic",
               |  "payload": $message
               |}""".stripMargin

        mediator ! Publish(topic, event)
    }
}
