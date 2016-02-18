package traffic.brokers

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.scalalogging.LazyLogging
import play.api.Play.current
import play.api.libs.concurrent.Akka


class MediatorBroker extends MessageBroker with LazyLogging {

    val mediator = DistributedPubSub(Akka.system).mediator

    logger.debug(s"Akka.system.name = ${Akka.system.name}")

    override def send(topic: String, message: String): Unit = {
        // logger.debug(s"$topic: $message")
        mediator ! Publish(topic, message)
    }

}
