package traffic.protocol

import play.api.libs.json.Json
import traffic.model.Subscriber

case class AttachEvent(bearerId: String, subscriber: Subscriber, topic: String = "attach-topic")
    extends TopicEvent(topic)

object AttachEvent {
    implicit val f = Json.format[AttachEvent]
}

