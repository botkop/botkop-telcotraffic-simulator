package traffic.actors

import akka.actor.{ActorLogging, Actor, Props}
import traffic.model.{Celltower, Trip}
import play.api.libs.json.Json
import traffic.actors.CelltowerLocationHandler.HandleCelltowerLocation
import traffic.brokers.MessageBroker

class CelltowerLocationHandler(mcc: Int, mnc: Int, broker: MessageBroker) extends Actor with ActorLogging {

    override def receive = {
        case HandleCelltowerLocation(trip) =>
            handleCelltowerLocation(trip)
    }

    def handleCelltowerLocation(trip: Trip): Unit = {
        trip.currentLocation match {
            case Some(location) =>
                val celltower: Celltower = Celltower.getNearest(mcc, mnc, location)
                broker.send(Json.stringify(Json.toJson(celltower)))
            case None =>
                log.error("unable to obtain location")
        }
    }

}

object CelltowerLocationHandler {
    case class HandleCelltowerLocation(trip: Trip)
    def props(mcc: Int, mnc: Int, broker: MessageBroker) = Props(new CelltowerLocationHandler(mcc, mnc, broker))
}
