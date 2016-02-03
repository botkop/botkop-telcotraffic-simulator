package traffic.actors

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.routing.{Broadcast, ConsistentHashingPool}
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import squants.motion.KilometersPerHour
import squants.time.Milliseconds
import traffic.actors.TripHandler.{StopTrip, StartTrip}
import traffic.model._
import traffic.protocol.{RequestEvent, RequestUpdateEvent}

class TrafficSimulator() extends Actor with ActorLogging {

    val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe("request-topic", self)

    var currentRequest: RequestEvent = _

    val conf = current.configuration.getConfig("traffic.cluster.router.pool").get
    val settings = ClusterRouterPoolSettings(
        totalInstances = conf.getInt("total-instances").get,
        maxInstancesPerNode = conf.getInt("max-instances-per-node").get,
        allowLocalRoutees = conf.getBoolean("allow-local-routees").get,
        useRole = conf.getString("use-role"))
    val pool = ClusterRouterPool(ConsistentHashingPool(0), settings)

    var router: ActorRef = context.actorOf(Props.empty)

    def startSimulation(json: JsValue) = {

        currentRequest = (json \ "request").as[RequestEvent]

        // stop running simulation before starting a new one
        stopSimulation()

        log.info("starting simulation")

        val slide = Milliseconds(currentRequest.slide)
        val velocity = KilometersPerHour(currentRequest.velocity)

        // get router configuration from config file
        // this would be ideal, but I do not know how to obtain a configuration without specifying a name
        // and if I set a name on the actor, then I can only use it once
        // val routerProps = FromConfig.props(Props[TripHandler])

        // still do not understand why we sometimes can re-use the name of this actor
        // also don't understand why declaring the router globally in the class only works for 1 request
        // val router = context.actorOf(routerProps, "TripRouter")

        router = context.actorOf(pool.props(TripHandler.props()))

        log.info("starting simulation")
        for (i <- 1 to currentRequest.numTrips) {
            val trip = Trip.random(currentRequest.mcc, currentRequest.mnc, velocity, slide)
            router ! StartTrip(trip)
        }

    }

    def stopSimulation() = {
        log.info("stopping simulation")
        router ! Broadcast(StopTrip)
    }

    def updateSimulation(json: JsValue) = {
        val r = (json \ "request").as[RequestUpdateEvent]
        log.info("request update event: " + r.toString)
        router ! Broadcast(r)
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

