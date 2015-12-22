package geo

case class LatLng(lat: Double, lng: Double) {

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

    def toJson: String = s"""{"lat":$lat,"lng":$lng}"""

}

object LatLng {
    def apply(): LatLng = LatLng(0.0, 0.0)
}
