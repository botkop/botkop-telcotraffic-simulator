package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.routing.BalancingPool
import play.api.libs.json.{JsValue, Json}
import squants.motion.KilometersPerHour
import squants.time.Milliseconds
import traffic.actors.TripHandler.StartTrip
import traffic.model._
import traffic.protocol.RequestEvent

class TrafficSimulator() extends Actor with ActorLogging {

    val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe("request-topic", self)

    def startSimulation(json: JsValue) = {

        val r = (json \ "request").as[RequestEvent]

        // stop running simulation before starting a new one
        stopSimulation()

        log.info("starting simulation")

        val slide = Milliseconds(r.slide)
        val velocity = KilometersPerHour(r.velocity)

        val tripHandlerPool =
            context.actorOf(new BalancingPool(r.numTrips).props(TripHandler.props(r.mcc, r.mnc, slide)))

        log.info("starting simulation")
        for (i <- 1 to r.numTrips) {
            val trip = Trip.random(r.mcc, r.mnc, velocity)
            tripHandlerPool ! StartTrip(trip)
        }
    }

    def stopSimulation() = {
        log.info("stopping simulation")
        context.children.foreach(context.stop)
    }

    def interpreteRequest(json: JsValue) = {
        log.debug(Json.stringify(json))
        val action = (json \ "action").as[String]
        action match {
            case "start" => startSimulation(json)
            case "stop" => stopSimulation()
        }
    }

    override def receive: Receive = {
        /*
        received from mediator: parse message and execute actions
        */
        case request: JsValue => interpreteRequest(request)
    }
}

object TrafficSimulator {
    def props() = Props(new TrafficSimulator())
}

