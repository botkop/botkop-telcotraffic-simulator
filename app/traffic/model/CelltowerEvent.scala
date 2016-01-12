package traffic.model

import play.api.libs.json.Json

case class CelltowerEvent(celltower: Celltower, bearerId: String, metrics: Map[String, Double])
object CelltowerEvent {
    implicit val f = Json.format[CelltowerEvent]
}

