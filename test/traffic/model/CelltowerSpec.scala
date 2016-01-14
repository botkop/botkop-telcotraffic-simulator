package traffic.model

import com.typesafe.scalalogging.LazyLogging
import geo.LatLng
import org.scalatest.{FlatSpec, Matchers}
import play.api.test.WithApplication
import traffic.FakeTestApp


class CelltowerSpec extends FlatSpec with Matchers with LazyLogging {

    "Celltower" should "return all celltowers" in new WithApplication(FakeTestApp()) {
        val all = Celltower.getAll(206, 10)
        all.length should be > 30000
    }

    "Celltower" should "return 1 celltower" in new WithApplication(FakeTestApp()) {
        val one = Celltower.getOne(206, 10, 12823, 16200).get

        one.location.distanceFrom(LatLng(50.863819, 4.32918)) should be < 500.0

    }

    "Celltower" should "return 5 getRandom celltowers" in new WithApplication(FakeTestApp()) {
        val five = Celltower.getRandom(206, 10, 5)
        five.length should be (5)
    }

}
