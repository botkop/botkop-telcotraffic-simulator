package traffic.model

import play.api.libs.json.Json

case class SimulatorRequest(mcc: Int, mnc: Int, numTrips: Int, slide: Double, velocity: Double, speedFactor: Double)

object SimulatorRequest {
    implicit val r = Json.reads[SimulatorRequest]
    implicit val w = Json.writes[SimulatorRequest]
}
