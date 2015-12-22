package geo

import org.scalatest.{FlatSpec, Matchers}

class LatLngSpec extends FlatSpec with Matchers {

    "A LatLng" should "calculate the distance" in {
        val from = LatLng(38.898556, -77.037852)
        val to = LatLng(38.897147, -77.043934)
        549.0 should be (Math.floor(from.distanceFrom(to)))
    }

    it should "calculate distance to same point as 0" in {
        val from = LatLng(38.898556, -77.037852)
        0 should be (Math.floor(from.distanceFrom(from)))
    }

}
