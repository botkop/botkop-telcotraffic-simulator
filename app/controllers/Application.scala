package controllers

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import play.api.Configuration
import play.api.Play.current
import play.api.libs.json.{JsError, JsResultException, Json}
import play.api.mvc._
import traffic.actors.{SimulatorSocketHandler, TrafficSimulator}
import traffic.brokers.MessageBroker
import traffic.helpers.JsonMessageParser

class Application extends Controller with LazyLogging {

    val system = ActorSystem("TrafficSimulatorSystem")

    val broker = initBroker

    val trafficSimulator = system.actorOf(TrafficSimulator.props(broker))

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

    /**
      * handle REST requests
      * @return
      */
    def restRequest = Action(BodyParsers.parse.json) { request =>
        try {
            trafficSimulator ! JsonMessageParser.interpreteJson(request.body)
            Ok(Json.obj("status" -> "OK"))
        } catch {
            case jre: JsResultException =>
                BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toJson(jre.errors)))
            case e: Exception =>
                BadRequest(Json.obj("status" -> "OK", "message" -> e.getMessage))
        }
    }

    /**
      * Handle HTTP requests
      * @return
      */
    def simulatorPage() = Action { implicit request =>
        Ok(views.html.simulator(request))
    }

    /**
      * Socket for HTTP clients
      * @return
      */
    def simulatorSocket() = WebSocket.acceptWithActor[String, String] { req => out =>
        SimulatorSocketHandler.props(out, trafficSimulator)
    }

}
