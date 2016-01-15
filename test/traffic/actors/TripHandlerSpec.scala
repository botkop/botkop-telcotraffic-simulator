package traffic.actors

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.scalalogging.LazyLogging
import geo.LatLng
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
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
    }

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

    val mcc = 206
    val mnc = 10
    val slide = Milliseconds(250)

    def makeTrip(kmh: Double = 1200): Trip = {
        val route = Route("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
        val velocity: Velocity = KilometersPerHour(kmh)
        Trip(sub, route, velocity)
    }

    def this() = this(ActorSystem("TripHandlerSpec"))

    override def afterAll() {
        TestKit.shutdownActorSystem(system)
    }

    "A TripHandler" must {

        "emit celltower messages along the route" in new WithApplication(FakeTestApp()) {

            val route = "_zpvHkvvUpA@HcCsLaB_AKQAs@SCcFAkA?mB@OTc@HM~@e@tDiB`BaAnGaDdDaBh@YFKh@[d@i@z@{A`@gA\\w@^m@d@k@d@a@vIoE~IuE|QoJjG}CpCiAxAa@t@MrAKnACf@@hAHjAPt@PfA\\`ChA~CnBrKlHzPpLdFhDrGxDVFf@ZhDlBhK~F|GzD~ErCnI|EfCzA^^\\f@\\~@Lj@b@hEdAzJNzBJfB@xAIdDUbDEn@@h@yBnOaClPaFd]wBpN_RjlAeEtXuC~QwBlMoDlRqDrRiMbq@uNpu@wHda@}Gd^yCtO}CtPyA|HaH`^mMvq@iG~[iGf\\iCpNkBvLuB|OuCpTkHri@_WpmBmNdfAyNxgA_Hbh@eE~[eMphAaHfn@q@zFmA~KqB`QeDtYuAlMgMniAyItv@mEj\\cXzpBc]bgCiDbVsA|HuChOyD~P[nBiAlEaCvIsPnj@_Vbx@k}@lzCuGnTuF|QgE|NwHlW{GxTcNhe@oe@h`BoEvOgA~DkAzE_BvHgEbUmCtN}DhTcW`uAmTfkAmRtdA{i@nxCubA`qF}VduAa@rBcCvM_CdLeB~G{BdIiCfIgBbFoC`HyBdF_FhKuP~]mHtOcCzFmBfFaBtEiBzFwAdF{@bDaAvDaCxKSbAm@`DoArHmBdNe@bEqBbQy@fHgSdfBkPbwAm@xE}A`LmBfMyBfMqC|NqErSiJ|_@uHd[gMlh@aL`e@wUz`AwTd~@}B|JmDrQiAxGwCxScAvI{E|b@mAtM{@vJc@|F]~HGlDAbGHzFHhCHnB^rFb@lEr@dF`ArFxEzUpD`RJh@x@lEzCzRv@rGrPbmAbClQjOvgAhBpKnAjGlAdFnB`H`EbNlTpt@|BfIdBhHv@nDdA|FjAnHbAxHx@vHxAfQdKvrAnMjaBvBzTdAhJvBrPzCvRzArI~BvLfEpRXjArArFfCrJ~Ojj@|@pC`DfL~AnFDt@Lh@T|AD~@Ez@O~@Sh@[b@m@^e@Dg@Ga@Y]g@Si@]eAEg@Cm@@a@Lw@dEmI|@aBnCkF`A}ATa@h@eArB{DnCuFb@qAfKaSTY|AuCzCeGnDeHpNqXxQo]h[{l@`i@ccA|Ra_@LKlA_CTa@f@x@jB~EpGrP~@vCb@EvGaCx@OJILMrDcGbBsC~BsDnB_DlAsA`@]nAw@fCsA|DkAxCe@"
            val trip = Trip(sub, Route(route), KilometersPerHour(120))

            val broker = new ActorBroker(system, "celltower-topic")
            val tripHandler = system.actorOf(TripHandler.props(mcc, mnc, slide, broker))
            tripHandler ! StartTrip(trip)
            val seq: Seq[String] = receiveN(8, 2000.millis).asInstanceOf[Seq[String]]
            tripHandler ! PoisonPill

            logger.info(seq.toString())

            val events: Seq[CelltowerEvent] = seq flatMap { Json.parse(_).asOpt[CelltowerEvent] }

            // we can't test for the exact celltower, since the database may change.
            // so we test the approximate distance
            val dist = events.last.celltower.location.distanceFrom(LatLng(51.045604,3.725985))
            logger.debug("distance from reference: {}", dist.toString)
            dist should be < 500.0 // less than 500 meters
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
