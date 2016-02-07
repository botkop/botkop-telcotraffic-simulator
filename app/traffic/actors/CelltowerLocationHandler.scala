package traffic.actors

import java.util
import java.util.UUID

import akka.actor._
import breeze.stats.distributions.Gaussian
import play.api.Configuration
import play.api.Play.current
import traffic.actors.CelltowerEventHandler.{EmitCelltowerAttachEvent, EmitCelltowerEvent}
import traffic.model.{Celltower, CelltowerCache, Trip}

import scala.collection.JavaConversions._

case class MetricTemplate(name: String, dist: Gaussian, anomalyFreq: Int)
case class CelltowerTemplate(name: String, metrics: List[MetricTemplate])

class CelltowerLocationHandler() extends Actor with ActorLogging {

    import CelltowerLocationHandler._

    var cache: Option[CelltowerCache] = None
    def celltowerCache(mcc: Int, mnc: Int): CelltowerCache = cache match {
        case None =>
            cache = Some(CelltowerCache(mcc, mnc))
            cache.get
        case _ =>
            cache.get
    }

    val templatesConfig = current.configuration.getConfigList("celltower-templates").get

    val templates: List[CelltowerTemplate] = templatesConfig.map { template =>
        val templateName = template.getString("name").get
        val metricsConfig: util.List[Configuration] = template.getConfigList("metrics").get

        val metrics = for (
            mc: Configuration <- metricsConfig;
            metricName = mc.getString("name").get;
            mean = mc.getDouble("mean").get;
            std = mc.getDouble("std").get;
            anomalyFreq = mc.getInt("anomalyFrequency").getOrElse(0);
            metricTemplate = MetricTemplate(metricName, Gaussian(mean, std), anomalyFreq)
        ) yield metricTemplate
        CelltowerTemplate(templateName, metrics.toList)
    }.toList

    log.debug("read the following celltower templates: {}", templates.toString)

    var celltowerActorMap = Map.empty[Celltower, ActorRef]

    def getCelltowerActor(celltower: Celltower) = celltowerActorMap.getOrElse(celltower, {
        val templateIdx = celltower.cell % templates.length
        val template = templates(templateIdx)
        val actor = context actorOf Props(new CelltowerEventHandler(celltower, template))
        celltowerActorMap += celltower -> actor
        context watch actor
        actor
    })

    var tripMap = Set.empty[UUID]

    override def receive = {
        case HandleCelltowerLocation(trip) =>
            handleCelltowerLocation(trip)
        case Terminated(ref) =>
            // clean up celltowerActorMap
            celltowerActorMap = celltowerActorMap filterNot { case (_, v) => v == ref }
    }

    def handleCelltowerLocation(trip: Trip): Unit = {
        trip.currentLocation match {
            case Some(location) =>
                val celltower: Celltower = celltowerCache(trip.mcc, trip.mnc).getNearest(location)
                val celltowerActor = getCelltowerActor(celltower)
                celltowerActor ! EmitCelltowerEvent(trip)

                if (! tripMap(trip.bearerId)) {
                    log.debug("emitting attach event for {}", trip.bearerId.toString)
                    celltowerActor ! EmitCelltowerAttachEvent(trip)
                    tripMap = tripMap + trip.bearerId
                }

            case None =>
                log.error("unable to obtain location")
        }
    }

}

object CelltowerLocationHandler {
    case class HandleCelltowerLocation(trip: Trip)
    def props() = Props(new CelltowerLocationHandler())
}

