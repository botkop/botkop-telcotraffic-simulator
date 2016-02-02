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
import traffic.protocol.{RequestUpdateEvent, RequestEvent}

class TrafficSimulator() extends Actor with ActorLogging {

    val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe("request-topic", self)

    var currentRequest: RequestEvent = _

    def startSimulation(json: JsValue) = {

        currentRequest = (json \ "request").as[RequestEvent]

        // stop running simulation before starting a new one
        stopSimulation()

        log.info("starting simulation")

        val slide = Milliseconds(currentRequest.slide)
        val velocity = KilometersPerHour(currentRequest.velocity)

        val tripHandlerPool = context.actorOf(
            new BalancingPool(currentRequest.numTrips).props(TripHandler.props()))

        log.info("starting simulation")
        for (i <- 1 to currentRequest.numTrips) {
            val trip = Trip.random(currentRequest.mcc, currentRequest.mnc, velocity, slide)
            tripHandlerPool ! StartTrip(trip)
        }
    }

    def stopSimulation() = {
        log.info("stopping simulation")
        context.children.foreach(context.stop)
    }

    def updateSimulation(json: JsValue) = {
        val r = (json \ "request").as[RequestUpdateEvent]
        log.info("request update event: " + r.toString)
        context.children.foreach(_ ! r)
    }

    def interpreteRequest(json: JsValue) = {
        log.debug(Json.stringify(json))
        val action = (json \ "action").as[String]
        action match {
            case "start" => startSimulation(json)
            case "update" => updateSimulation(json)
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

