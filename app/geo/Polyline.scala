package geo

import scala.collection.mutable.ListBuffer

case class Polyline(path: List[LatLng], polyline: String) {

    def distance(): Double = {
        var dist = 0.0
        for ( i <- 1 until path.size) {
            dist += path(i).distanceFrom(path(i-1))
        }
        dist
    }

    def pointAtDistance(metres: Double): Option[LatLng] = {
        if (metres == 0) return Some(path.head)
        if (metres < 0) return None
        if (path.size < 2) return None

        var dist = 0.0
        var oldDist = 0.0

        var i = 1
        while (i < path.size && dist < metres) {
            oldDist = dist
            dist += path(i).distanceFrom(path(i-1))
            i += 1
        }

        if (dist < metres) {
            return Some(path.last)
        }

        val p1 = path(i-2)
        val p2 = path(i-1)
        val m = (metres-oldDist)/(dist-oldDist)

        val location = LatLng( p1.lat + (p2.lat-p1.lat)*m, p1.lng + (p2.lng-p1.lng)*m)
        Some(location)
    }
}

object Polyline {

    def apply(polyline: String, precision: Double) = {
        new Polyline(decode(polyline, precision), polyline)
    }

    def apply(path: List[LatLng], precision: Double) = {
        new Polyline(path, encode(path, precision))
    }

    def apply(polyline: String) = { // Scala does not allow overloading with default args
        new Polyline(decode(polyline, 0.0), polyline)
    }

    def apply(path: List[LatLng]) = { // Scala does not allow overloading with default args
        new Polyline(path, encode(path, 0.0))
    }

    def decode(str: String, precision: Double = 0.0) : List[LatLng] = {
        var index = 0
        var lat = 0.0
        var lng = 0.0
        var coordinates = new ListBuffer[LatLng]()
        var shift = 0
        var result = 0
        var byte = 0
        var latitudeChange = 0.0
        var longitudeChange = 0.0
        val factor = Math.pow(10, if (precision == 0.0) 5 else precision)

        // Coordinates have variable length when encoded, so just keep
        // track of whether we've hit the end of the string. In each
        // loop iteration, a single coordinate is decoded.

        while (index < str.length) {

            // Reset shift, result, and byte
            byte = 0
            shift = 0
            result = 0

            do {
                byte = str(index) - 63
                index += 1
                result |= (byte & 0x1f) << shift
                shift += 5
            } while (byte >= 0x20)

            latitudeChange = if ((result & 1) != 0) ~(result >> 1) else result >> 1

            shift = 0
            result = 0

            do {
                byte = str(index) - 63
                index += 1
                result |= (byte & 0x1f) << shift
                shift += 5
            } while (byte >= 0x20)

            longitudeChange = if ((result & 1) != 0) ~(result >> 1) else result >> 1

            lat += latitudeChange
            lng += longitudeChange

            coordinates += LatLng(lat / factor, lng / factor)

        }

        coordinates.toList
    }

    def encode(coordinates: List[LatLng], precision: Double = 0.0): String = {

        if (coordinates.isEmpty) {
            return ""
        }

        val factor = Math.pow(10, if (precision == 0.0) 5 else precision)
        var output: String = encode(coordinates.head.lat, factor) + encode(coordinates.head.lng, factor)

        for (i <- 1 until coordinates.size) {
            val a = coordinates(i)
            val b = coordinates(i-1)
            output += encode(a.lat - b.lat, factor)
            output += encode(a.lng - b.lng, factor)
        }

        output
    }

    private def encode(coord: Double, factor: Double): String = {

        var coordinate: Int = Math.round(coord * factor).toInt
        coordinate = coordinate << 1
        if (coordinate < 0) {
            coordinate = ~coordinate
        }

        var output = ListBuffer[Int]()
        while (coordinate >= 0x20) {
            output += (0x20 | (coordinate & 0x1f)) + 63
            coordinate >>= 5
        }
        output += coordinate + 63

        new String(output.toArray, 0, output.length)
    }

}
