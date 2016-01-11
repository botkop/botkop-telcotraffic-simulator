package traffic.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import breeze.stats.distributions.Gaussian
import play.api.Configuration
import play.api.Play.current
import play.api.libs.json.Json
import traffic.brokers.MessageBroker
import traffic.model.{Celltower, CelltowerCache, Trip}

import scala.collection.JavaConversions._

class CelltowerLocationHandler(mcc: Int, mnc: Int, broker: MessageBroker) extends Actor with ActorLogging {

    import CelltowerLocationHandler._

    val topicName = "celltower-topic"
    val celltowerCache = CelltowerCache(mcc, mnc)

    case class MetricTemplate(name: String, dist: Gaussian)
    case class CelltowerTemplate(name: String, metrics: List[MetricTemplate])

    val templatesConfig = current.configuration.getConfigList("celltower-templates").get

    val templates: List[CelltowerTemplate] = templatesConfig.map { template =>
        val templateName = template.getString("name").get
        val metricsConfig = template.getConfigList("metrics").get

        val metrics = for (
            mc: Configuration <- metricsConfig;
            metricName = mc.getString("name").get;
            mean = mc.getDouble("mean").get;
            std = mc.getDouble("std").get;
            metricTemplate = MetricTemplate(metricName, Gaussian(mean, std))
        ) yield metricTemplate

        CelltowerTemplate(templateName, metrics.toList)
    }.toList

    log.debug("read the following celltower templates: {}", templates.toString)

    case class CelltowerEvent(celltower: Celltower, bearerId: String, metrics: Map[String, Double])
    object CelltowerEvent {
        implicit val w = Json.writes[CelltowerEvent]
    }

    override def receive = {
        case HandleCelltowerLocation(trip) =>
            handleCelltowerLocation(trip)
    }

    def buildEvent(celltower: Celltower, bearerId: UUID): String = {

        // which template to use for this celltower
        // template should always be the same for the same celltower
        // here we are using cell id mod (templates.length)
        // which avoids the instantiation of an actor/object for each celltower

        val templateIdx = celltower.cell % templates.length

        val templateMetrics = templates(templateIdx).metrics

        val metrics = templateMetrics.map { mt =>
            (mt.name, mt.dist.sample())
        }.toMap

        val celltowerEvent = CelltowerEvent(celltower, bearerId.toString, metrics)
        Json.stringify(Json.toJson(celltowerEvent))
    }

    def handleCelltowerLocation(trip: Trip): Unit = {
        trip.currentLocation match {
            case Some(location) =>
                val celltower: Celltower = celltowerCache.getNearest(location)
                broker.send(topicName, buildEvent(celltower, trip.bearerId))
            case None =>
                log.error("unable to obtain location")
        }
    }

}

object CelltowerLocationHandler {
    case class HandleCelltowerLocation(trip: Trip)
    def props(mcc: Int, mnc: Int, broker: MessageBroker) = Props(new CelltowerLocationHandler(mcc, mnc, broker))
}

