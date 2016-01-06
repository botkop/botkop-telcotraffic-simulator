package traffic.actors

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.test.WithApplication
import squants.Velocity
import squants.motion.KilometersPerHour
import squants.time.Milliseconds
import traffic.FakeTestApp
import traffic.brokers.MessageBroker
import traffic.model._

import scala.concurrent.duration._
import scala.language.postfixOps

class TripHandlerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with LazyLogging {

    import TripHandler._

    // TODO: make an actor per topic
    class ActorBroker(system: ActorSystem) extends MessageBroker {

        object Bubble {
            def props = Props(new Bubble)

            class Bubble extends Actor {
                override def receive = {
                    case msg => sender ! msg
                }
            }

        }

        val actor: ActorRef = system.actorOf(Bubble.props)

        override def send(topic: String, message: String): Unit = {
            actor ! message
        }

        override def configure(config: Configuration): Unit = {}
    }

    def makeTrip(): Trip = {
        val route = Route("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
        val sub = Subscriber(
            id = 1,
            imsi = "imsi",
            msisdn = "msisdn",
            imei = "imei",
            lastName = "lastName",
            firstName = "firstName",
            address = "address",
            city = "city",
            zip = "zip",
            country = "country"
        )
        val velocity: Velocity = KilometersPerHour(1200)
        Trip(sub, route, velocity)
    }

    def this() = this(ActorSystem("TripHandlerSpec"))

    override def afterAll() {
        TestKit.shutdownActorSystem(system)
    }

    "A TripHandler" must {

        "emit subscriber and celltower messages along the route" in new WithApplication(FakeTestApp()) {

            val trip = makeTrip()
            val mcc = 206
            val mnc = 10
            val slide = Milliseconds(250)

            val broker = new ActorBroker(system)
            val tripHandler = system.actorOf(TripHandler.props(mcc, mnc, slide, broker))

            tripHandler ! ContinueTrip(trip)
            val seq: Seq[String] = receiveN(8, 2000.millis).asInstanceOf[Seq[String]]
            // logger.info(seq.toString())

            var subList = List[SubscriberLocation]()
            var cellList = List[Celltower]()

            // sequence contains both celltower and subscriber locations
            // parse and store them in separate lists
            seq.foreach { r =>
                val p: JsValue = Json.parse(r)
                // is it a subscriber location ?
                p.asOpt[SubscriberLocation] match {
                    case slo: Some[SubscriberLocation] =>
                        subList = subList :+ slo.get
                    case None =>
                        // is it a celltower ?
                        p.asOpt[Celltower] match {
                            case cto: Some[Celltower] =>
                                cellList = cellList :+ cto.get
                            case None =>
                                logger.error(s"unknown message received: $r")
                        }
                }
            }

            // TODO: make this a better test for actors per topic

            logger.info(subList.toString)
            // subList.length should be >= 4
            // subList.last.location should be (LatLng(38.505068301565004,-120.20172783007898))

            logger.info(cellList.toString)
            // cellList.length should be >= 2
        }
    }

}
