package traffic.model

import play.api.libs.json.Json

case class SimulatorRequest(mcc: Int, mnc: Int, numTrips: Int, slide: Double, velocity: Double)

object SimulatorRequest {
    implicit val requestWrites = Json.writes[SimulatorRequest]
    implicit val requestReads = Json.reads[SimulatorRequest]
}

