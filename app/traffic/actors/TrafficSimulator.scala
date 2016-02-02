package traffic.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.routing.{ConsistentHashingPool, FromConfig}
import play.api.libs.json.{JsValue, Json}
import squants.motion.KilometersPerHour
import squants.time.Milliseconds
import traffic.actors.TripHandler.StartTrip
import traffic.model._
import traffic.protocol.{RequestEvent, RequestUpdateEvent}

class TrafficSimulator() extends Actor with ActorLogging {

    val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe("request-topic", self)

    var currentRequest: RequestEvent = _


    // get router configuration from config file
    val routerProps = FromConfig.props(Props[TripHandler])

    // alternatively we could code it:
    /*
    val settings = ClusterRouterPoolSettings(
        totalInstances = 10,
        maxInstancesPerNode = 3,
        allowLocalRoutees = true,
        useRole = Some("simulate"))

    val pool = ClusterRouterPool(ConsistentHashingPool(0), settings)

    val router = context.actorOf(pool.props(Props[TripHandler]))
    */

    def startSimulation(json: JsValue) = {

        currentRequest = (json \ "request").as[RequestEvent]

        // stop running simulation before starting a new one
        stopSimulation()

        log.info("starting simulation")

        val slide = Milliseconds(currentRequest.slide)
        val velocity = KilometersPerHour(currentRequest.velocity)

        // still do not understand why we can re-use the name of this actor
        // (in most cases)
        // also don't understand why declaring the router globally in the class does not work
        val router = context.actorOf(routerProps, "TripRouter")

        log.info("starting simulation")
        for (i <- 1 to currentRequest.numTrips) {
            val trip = Trip.random(currentRequest.mcc, currentRequest.mnc, velocity, slide)
            router ! StartTrip(trip)
        }

    }

    def stopSimulation() = {
        log.info("stopping simulation")
        context.children.foreach(context.stop)
    }

    def updateSimulation(json: JsValue) = {
        val r = (json \ "request").as[RequestUpdateEvent]
        log.info("request update event: " + r.toString)

        // should probably use a Broadcast here
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

