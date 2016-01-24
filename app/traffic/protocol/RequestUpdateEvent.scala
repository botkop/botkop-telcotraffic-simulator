package traffic.protocol

import play.api.libs.json.Json

case class RequestUpdateEvent(slide: Option[Double], velocity: Option[Double], topic: String = "request-topic")
    extends TopicEvent(topic)

object RequestUpdateEvent {
    implicit val f = Json.format[RequestUpdateEvent]
}
