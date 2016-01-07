package traffic.actors

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.scalalogging.LazyLogging
import geo.LatLng
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.Configuration
import play.api.libs.json.Json
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

    class ActorBroker(system: ActorSystem, useTopic: String) extends MessageBroker {

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
            // filter per topic
            if  (useTopic == topic) {
                actor ! message
            }
        }

        override def configure(config: Configuration): Unit = {} // no config required
    }

    def makeTrip(kmh: Double = 1200): Trip = {
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
        val velocity: Velocity = KilometersPerHour(kmh)
        Trip(sub, route, velocity)
    }

    def this() = this(ActorSystem("TripHandlerSpec"))

    override def afterAll() {
        TestKit.shutdownActorSystem(system)
    }

    "A TripHandler" must {

        val mcc = 206
        val mnc = 10
        val slide = Milliseconds(250)

        "emit celltower messages along the route" in new WithApplication(FakeTestApp()) {

            val trip = makeTrip()
            val broker = new ActorBroker(system, "celltower-topic")
            val tripHandler = system.actorOf(TripHandler.props(mcc, mnc, slide, broker))
            tripHandler ! ContinueTrip(trip)
            val seq: Seq[String] = receiveN(8, 2000.millis).asInstanceOf[Seq[String]]
            tripHandler ! PoisonPill

            logger.info(seq.toString())

            val cl: Seq[Celltower] = seq flatMap { Json.parse(_).asOpt[Celltower] }

            cl.last should be (Celltower(206,10,62584,9731,LatLng(48.433702,-4.444959)))
        }

        "emit subscriber messages along the route" in new WithApplication(FakeTestApp()) {

            val trip = makeTrip()
            val broker = new ActorBroker(system, "subscriber-topic")
            val tripHandler = system.actorOf(TripHandler.props(mcc, mnc, slide, broker))
            tripHandler ! ContinueTrip(trip)
            val seq: Seq[String] = receiveN(8, 2000.millis).asInstanceOf[Seq[String]]
            tripHandler ! PoisonPill

            logger.info(seq.toString())

            val sl: Seq[SubscriberLocation] = seq flatMap { Json.parse(_).asOpt[SubscriberLocation] }

            sl.last.location should be (LatLng(38.50579234464571,-120.2019746629474))
        }

        "allow to control the speed factor" in new WithApplication(FakeTestApp()) {

            val trip = makeTrip(120)
            val broker = new ActorBroker(system, "subscriber-topic")
            val tripHandler = system.actorOf(TripHandler.props(mcc, mnc, slide, broker))
            tripHandler ! SetSpeedFactor(10)
            tripHandler ! ContinueTrip(trip)
            val seq: Seq[String] = receiveN(8, 2000.millis).asInstanceOf[Seq[String]]
            tripHandler ! PoisonPill

            logger.info(seq.toString())

            val sl: Seq[SubscriberLocation] = seq flatMap { Json.parse(_).asOpt[SubscriberLocation] }

            sl.last.location should be (LatLng(38.50579234464571,-120.2019746629474))
        }

    }

}
