package traffic.model

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Matchers}
import play.api.test.WithApplication
import traffic.FakeTestApp


class SubscriberSpec extends FlatSpec with Matchers with LazyLogging {

    "Subscriber" should "return 1 subscriber" in new WithApplication(FakeTestApp()) {
        val one = Subscriber.getOne(20).get
        one.msisdn should be ("475506986")
        one.imsi should be ("3666489459")
        one.imei should be ("571761800656171")
        one.firstName should be ("Brice")
        one.lastName should be ("Parenteau")
    }

    "Subscriber" should "return 5 getRandom subscribers" in new WithApplication(FakeTestApp()) {
        val five = Subscriber.random(5)
        five.length should be (5)
    }

}
