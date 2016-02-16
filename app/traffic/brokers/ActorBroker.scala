package traffic.brokers

import akka.actor.{ActorLogging, Actor, ActorRef, Props}

import scala.collection.mutable

class ActorBroker extends Actor with MessageBroker with ActorLogging {
    import ActorBroker._

    val receivers = new mutable.LinkedHashSet[ActorRef]()

    override def send(topic: String, message: String): Unit = {
        receivers.foreach(_ ! TopicMessage(topic, message))
    }

    override def receive: Receive =  {
        case SubscribeReceiver(receiverActor: ActorRef) =>
            log.info("received subscribe from %s".format(receiverActor.toString))
            receivers += receiverActor

        case UnsubscribeReceiver(receiverActor: ActorRef) =>
            log.info("received unsubscribe from %s".format(receiverActor.toString))
            receivers -= receiverActor
    }
}

object ActorBroker {
    def props(socket: ActorRef) = Props(new ActorBroker())

    case class SubscribeReceiver(receiverActor: ActorRef)
    case class UnsubscribeReceiver(receiverActor: ActorRef)
    case class TopicMessage(topic: String, message: String)

}
