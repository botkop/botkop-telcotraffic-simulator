package traffic.protocol

import java.util.Calendar

import play.api.libs.json.Json
import traffic.model.Celltower

case class CelltowerEvent(celltower: Celltower, bearerId: String, metrics: Map[String, Double],
                          topic: String = "celltower-topic",
                          ts: Long = Calendar.getInstance.getTimeInMillis
                         )
    extends TopicEvent(topic)

object CelltowerEvent {
    implicit val f = Json.format[CelltowerEvent]
}

