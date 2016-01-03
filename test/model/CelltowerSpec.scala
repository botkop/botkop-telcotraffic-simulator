package model

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Matchers}
import play.api.test.WithApplication


class CelltowerSpec extends FlatSpec with Matchers with LazyLogging {

    "Celltower" should "return all celltowers" in new WithApplication {
        val all = Celltower.getAll(206, 10)
        all.length should be (34988)
    }

    "Celltower" should "return 1 celltower" in new WithApplication {
        val one = Celltower.getOne(206, 10, 12823, 16200).get
        one.location.lat should be (50.863819)
        one.location.lng should be (4.32918)
    }

    "Celltower" should "return 5 getRandom celltowers" in new WithApplication {
        val five = Celltower.getRandom(206, 10, 5)
        five.length should be (5)
    }

}
