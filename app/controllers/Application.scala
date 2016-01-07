package controllers

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import play.api.Play.current
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import traffic.actors.TrafficSimulator.{StartSimulation, StopSimulation}
import traffic.actors.TripHandler.SetSpeedFactor
import traffic.actors.{SimulatorSocketHandler, TrafficSimulator}
import traffic.brokers.MessageBroker
import traffic.model.SimulatorRequest

class Application extends Controller with LazyLogging {

    val system = ActorSystem("TrafficSimulatorSystem")

    val broker: MessageBroker = initBroker

    val trafficSimulator = system.actorOf(TrafficSimulator.props(broker))

    def startSimulator = Action(BodyParsers.parse.json) { request =>
        val r = request.body.validate[SimulatorRequest]
        r.fold(
            errors => {
                BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toJson(errors)))
            },
            simulatorRequest => {
                trafficSimulator ! StartSimulation(simulatorRequest)

                Ok(Json.obj("status" -> "OK"))
            }
        )
    }

    def stopSimulator = Action { request =>
        trafficSimulator ! StopSimulation
        Ok(Json.obj("status" -> "OK"))
    }

    def setSpeedFactor() = Action(BodyParsers.parse.json) { request =>

        object SpeedRequest {
            case class SpeedRequest(speedFactor: Double)
            implicit val r = Json.reads[SpeedRequest]
        }
        import SpeedRequest._

        val r = request.body.validate[SpeedRequest]
        r.fold (
            errors => {
                BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toJson(errors)))
            },
            speedRequest => {
                val sf = speedRequest.speedFactor
                logger.info("setting speed factor to: {}", sf.toString)
                trafficSimulator ! SetSpeedFactor(sf)
                Ok(Json.obj("status" -> "OK"))
            }
        )
    }

    def simulatorPage() = Action { implicit request =>
        Ok(views.html.simulator(request))
    }

    def simulatorSocket() = WebSocket.acceptWithActor[String, String] { req => out =>
        SimulatorSocketHandler.props(out, trafficSimulator)
    }

    def initBroker: MessageBroker = {
        val conf = current.configuration
        val brokerName: String = conf.getString("messageBroker").get
        val brokerConfig = conf.getConfig(brokerName).get
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
