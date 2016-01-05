package geo

import play.api.libs.json.Json

case class LatLng(lat: Double = 0.0, lng: Double = 0.0) {

    val EarthRadiusMeters = 6378137 // meters

    def distanceFrom(to: LatLng): Double = {

        val dLat = (to.lat - this.lat) * Math.PI / 180
        val dLon = (to.lng - this.lng) * Math.PI / 180

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(this.lat * Math.PI / 180) * Math.cos(to.lat * Math.PI / 180) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        EarthRadiusMeters * c
    }

}

object LatLng {
    implicit val requestWrites = Json.writes[LatLng]
    implicit val requestReads = Json.reads[LatLng]
}
