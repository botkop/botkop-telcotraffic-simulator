package traffic.protocol

import play.api.libs.json.Json
import traffic.model.{Subscriber, Celltower}

case class CelltowerAttachEvent(celltower: Celltower, bearerId: String, subscriber: Subscriber, topic: String = "celltower-attach-topic")
    extends TopicEvent(topic)

object CelltowerAttachEvent {
    implicit val f = Json.format[CelltowerAttachEvent]
}

