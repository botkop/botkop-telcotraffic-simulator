package traffic.protocol

import play.api.libs.json.Json

case class RequestEvent(mcc: Int, mnc: Int, numTrips: Int, slide: Double, velocity: Double, speedFactor: Double, topic: String = "request-topic")
    extends TopicEvent(topic)

object RequestEvent {
    implicit val f = Json.format[RequestEvent]
}
