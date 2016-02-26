package traffic.protocol

import java.util.Calendar

import play.api.libs.json.Json
import traffic.model.Subscriber

case class AttachEvent(bearerId: String, subscriber: Subscriber,
                       topic: String = "attach-topic",
                       ts: Long = Calendar.getInstance.getTimeInMillis)
    extends TopicEvent(topic)

object AttachEvent {
    implicit val f = Json.format[AttachEvent]
}

