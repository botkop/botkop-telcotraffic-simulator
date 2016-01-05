package traffic.model

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Matchers}
import play.api.test.WithApplication


class SubscriberSpec extends FlatSpec with Matchers with LazyLogging {

    "Subscriber" should "return 1 subscriber" in new WithApplication {
        val one = Subscriber.getOne(20).get
        one.msisdn should be ("475506986")
        one.imsi should be ("3666489459")
        one.imei should be ("571761800656171")
        one.firstName should be ("Brice")
        one.lastName should be ("Parenteau")
    }

    "Subscriber" should "return 5 getRandom subscribers" in new WithApplication {
        val five = Subscriber.random(5)
        five.length should be (5)
    }

}
