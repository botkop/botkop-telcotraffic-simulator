package traffic.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}
import play.api.libs.json.{JsValue, Json}
import traffic.actors.SimulatorSocket.WebSocketEvent

class SimulatorSocket(socket: ActorRef) extends Actor with ActorLogging {

    val mediator = DistributedPubSub(context.system).mediator

    // subscribe to requests coming from REST or HTML interfaces
    mediator ! Subscribe("request-topic", self)

    // subscribe to events published by the WebSocketBroker
    // note: these events will only appear when the WebSocketBroker has been activated in the configuration
    mediator ! Subscribe("websocket-subscriber-topic", self)
    mediator ! Subscribe("websocket-celltower-topic", self)


    override def receive: Receive = {
        /*
        received from web ui
        transform to json and send to mediator
        */
        case message: String =>
            log.debug("received request: {}", message)
            val json: JsValue = Json.parse(message)
            mediator ! Publish("request-topic", json)

        /*
        received from mediator
        stringify and publish to all active web sockets, so the browsers can update their state
        */
        case request: JsValue =>
            socket ! Json.stringify(request)

        /*
        events emitted by the webSocketBroker: pass them on to the sockets
        note: these events will only appear when the WebSocketBroker has been activated in the configuration
        */
        case WebSocketEvent(event) =>
            socket ! event

    }
}

object SimulatorSocket {
    def props(socket: ActorRef) = Props(new SimulatorSocket(socket))
    case class WebSocketEvent(event: String)
}

