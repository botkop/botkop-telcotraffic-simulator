package traffic.actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.Configuration
import play.api.Play._
import traffic.model.Trip
import traffic.actors.CelltowerLocationHandler.HandleCelltowerLocation
import traffic.actors.SubscriberLocationHandler.HandleSubscriberLocation
import traffic.brokers.MessageBroker

class LocationHandler(mcc: Int, mnc: Int) extends Actor {

    import LocationHandler._

    val broker = initBroker

    val subscriberLocationHandler: ActorRef = context.actorOf(SubscriberLocationHandler.props(mcc, mnc, broker))
    val celltowerLocationHandler: ActorRef = context.actorOf(CelltowerLocationHandler.props(mcc, mnc, broker))

    override def receive: Receive = {

        case HandleLocation(trip: Trip) =>
            subscriberLocationHandler ! HandleSubscriberLocation(trip: Trip)
            celltowerLocationHandler ! HandleCelltowerLocation(trip: Trip)

    }

    /**
      * initialize message broker
      * @return
      */
    def initBroker: MessageBroker = {
        val conf = current.configuration
        val brokerName: String = conf.getString("messageBroker").get
        val brokerConfig: Configuration = conf.getConfig(brokerName).get
        val clazzName = brokerConfig.getString("class").get

        val broker = Class.forName(clazzName).newInstance.asInstanceOf[MessageBroker]

        brokerConfig.getConfig("properties") match {
            case Some(properties) =>
                broker.configure(properties)
            case _ =>
        }
        broker
    }

}

object LocationHandler {
    def props(mcc: Int, mnc: Int) =
        Props(new LocationHandler(mcc, mnc))
    case class HandleLocation(trip: Trip)
}
