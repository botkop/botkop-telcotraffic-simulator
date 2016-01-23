package traffic.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}
import play.api.libs.json.{JsValue, Json}
import traffic.protocol.{CelltowerEvent, SubscriberEvent}

class SimulatorSocket(socket: ActorRef) extends Actor with ActorLogging {

    val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe("request-topic", self)
    mediator ! Subscribe("subscriber-topic", self)
    mediator ! Subscribe("celltower-topic", self)

    override def receive: Receive = {

        /*
        received from web ui
        transform to json and send to mediator
        */
        case message: String =>
            log.debug("received message: {}", message)

            val json: JsValue = Json.parse(message)
            mediator ! Publish("request-topic", json)

        /*
        received from mediator
        stringify and publish to all active web sockets
        */
        case request: JsValue =>
            socket ! Json.stringify(request)

        /*
        events emitted by simulator: pass them on to the sockets
        */
        case event: SubscriberEvent =>
            socket ! Json.stringify(Json.toJson(event))

        case event: CelltowerEvent =>
            socket ! Json.stringify(Json.toJson(event))

    }
}

object SimulatorSocket {
    def props(socket: ActorRef) = Props(new SimulatorSocket(socket))
}

