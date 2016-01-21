package traffic.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}
import play.api.libs.json.{JsValue, Json}
import traffic.actors.SimulatorSocket.MediatingBrokerEvent

class SimulatorSocket(socket: ActorRef, simulator: ActorRef) extends Actor with ActorLogging {

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
            val json: JsValue = Json.parse(message)
            mediator ! Publish("request-topic", json)

        /*
        received from mediator
        stringify and publish to all active web sockets
        */
        case request: JsValue =>
            socket ! Json.stringify(request)

        case MediatingBrokerEvent(message) =>
            socket ! message

    }
}

object SimulatorSocket {
    def props(socket: ActorRef, simulator: ActorRef) = Props(new SimulatorSocket(socket, simulator))

    case class MediatingBrokerEvent(message: String)
}

