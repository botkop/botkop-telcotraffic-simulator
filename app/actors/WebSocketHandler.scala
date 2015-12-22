package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import model.Subscriber
import play.api.libs.json.{JsValue, Json}

class WebSocketHandler(out: ActorRef) extends Actor with ActorLogging {

    def engine(mcc: Int, mnc: Int, numTrips: Int) = {

        for( trip <- 1 to numTrips){
            val subscriber = Subscriber.random()

        }

    }

    def processMessage(message: String) = {
        val json: JsValue = Json.parse(message)
        val mcc = json \ "mcc"
        val mnc = json \ "mnc"
        val numTrips = json \ "numTrips"
        engine(mcc.as[Int], mnc.as[Int], numTrips.as[Int])
    }

    override def receive: Receive = {
        case message: String =>
            log.info("received " + message)
            processMessage(message)
            out ! message
        case _ =>
            log.info("received garbage")
    }
}

object WebSocketHandler {
    def props(out: ActorRef) = Props(new WebSocketHandler(out))
}
