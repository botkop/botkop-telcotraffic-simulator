package traffic.brokers

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.scalalogging.LazyLogging
import play.api.Play.current
import play.api.libs.concurrent.Akka
import traffic.actors.SimulatorSocket.WebSocketEvent

class WebSocketBroker extends MessageBroker with LazyLogging {

    val mediator = DistributedPubSub(Akka.system).mediator

    override def send(topic: String, message: String) = {
        mediator ! Publish(s"websocket-$topic", WebSocketEvent(message))
    }

}
