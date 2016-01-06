package traffic.model

import com.typesafe.scalalogging.LazyLogging
import geo.LatLng
import org.scalatest.{FlatSpec, Matchers}
import play.api.test.WithApplication
import traffic.FakeTestApp

/*
ignore this test to avoid access to google all the time
 */
// @Ignore
class RouteSpec extends FlatSpec with Matchers with LazyLogging {

    "A Route" should "get the directions from Google" in new WithApplication(FakeTestApp()) {

        val from = LatLng(64.1533981, -21.79994)
        val to = LatLng(63.96103710000001, -21.2614058)

        Route.byGoogle(from, to) match {

            case Some(route) =>
                val d = route.distance
                logger.info(s"distance = $d")
                d should be (46364.0 +- 1.0)

            case None => fail("unable to obtain route")

        }

    }

}
