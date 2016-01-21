package traffic.actors

import akka.actor._
import breeze.stats.distributions.Gaussian
import play.api.Configuration
import play.api.Play.current
import traffic.actors.CelltowerEventHandler.EmitCelltowerEvent
import traffic.model.{Celltower, CelltowerCache, Trip}

import scala.collection.JavaConversions._

case class MetricTemplate(name: String, dist: Gaussian)
case class CelltowerTemplate(name: String, metrics: List[MetricTemplate])

class CelltowerLocationHandler(mcc: Int, mnc: Int) extends Actor with ActorLogging {

    import CelltowerLocationHandler._

    val celltowerCache = CelltowerCache(mcc, mnc)

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

    var celltowerActorMap = Map.empty[Celltower, ActorRef]

    def getCelltowerActor(celltower: Celltower) = celltowerActorMap.getOrElse(celltower, {
        val templateIdx = celltower.cell % templates.length
        val template = templates(templateIdx)
        val actor = context actorOf Props(new CelltowerEventHandler(celltower, template))
        celltowerActorMap += celltower -> actor
        context watch actor
        actor
    })

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
                val celltower: Celltower = celltowerCache.getNearest(location)
                val celltowerActor = getCelltowerActor(celltower)
                celltowerActor ! EmitCelltowerEvent(trip.bearerId)
            case None =>
                log.error("unable to obtain location")
        }
    }

}

object CelltowerLocationHandler {
    case class HandleCelltowerLocation(trip: Trip)
    def props(mcc: Int, mnc: Int) = Props(new CelltowerLocationHandler(mcc, mnc))
}

