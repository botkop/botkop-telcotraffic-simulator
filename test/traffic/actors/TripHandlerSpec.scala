package traffic.actors

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import botkop.geo.LatLng
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.test.FakeApplication
import squants.motion.{MetersPerSecond, KilometersPerHour}
import squants.time.{Seconds, Milliseconds}
import traffic.model.{Route, Subscriber, Trip}
import traffic.protocol.{RequestUpdateEvent, CelltowerEvent, SubscriberEvent}

import scala.concurrent.Await
import scala.concurrent.duration._


/*
note: cannot use TestKit, because that comes with its own ActorSystem
that conflicts with the actor system from the application
 */
class TripHandlerSpec extends PlaySpec with LazyLogging with OneAppPerSuite {

    val defaultMcc = 206
    val defaultMnc = 10
    val defaultSlide = Milliseconds(250)
    val defaultRoute = Route("_zpvHkvvUpA@HcCsLaB_AKQAs@SCcFAkA?mB@OTc@HM~@e@tDiB`BaAnGaDdDaBh@YFKh@[d@i@z@{A`@gA\\w@^m@d@k@d@a@vIoE~IuE|QoJjG}CpCiAxAa@t@MrAKnACf@@hAHjAPt@PfA\\`ChA~CnBrKlHzPpLdFhDrGxDVFf@ZhDlBhK~F|GzD~ErCnI|EfCzA^^\\f@\\~@Lj@b@hEdAzJNzBJfB@xAIdDUbDEn@@h@yBnOaClPaFd]wBpN_RjlAeEtXuC~QwBlMoDlRqDrRiMbq@uNpu@wHda@}Gd^yCtO}CtPyA|HaH`^mMvq@iG~[iGf\\iCpNkBvLuB|OuCpTkHri@_WpmBmNdfAyNxgA_Hbh@eE~[eMphAaHfn@q@zFmA~KqB`QeDtYuAlMgMniAyItv@mEj\\cXzpBc]bgCiDbVsA|HuChOyD~P[nBiAlEaCvIsPnj@_Vbx@k}@lzCuGnTuF|QgE|NwHlW{GxTcNhe@oe@h`BoEvOgA~DkAzE_BvHgEbUmCtN}DhTcW`uAmTfkAmRtdA{i@nxCubA`qF}VduAa@rBcCvM_CdLeB~G{BdIiCfIgBbFoC`HyBdF_FhKuP~]mHtOcCzFmBfFaBtEiBzFwAdF{@bDaAvDaCxKSbAm@`DoArHmBdNe@bEqBbQy@fHgSdfBkPbwAm@xE}A`LmBfMyBfMqC|NqErSiJ|_@uHd[gMlh@aL`e@wUz`AwTd~@}B|JmDrQiAxGwCxScAvI{E|b@mAtM{@vJc@|F]~HGlDAbGHzFHhCHnB^rFb@lEr@dF`ArFxEzUpD`RJh@x@lEzCzRv@rGrPbmAbClQjOvgAhBpKnAjGlAdFnB`H`EbNlTpt@|BfIdBhHv@nDdA|FjAnHbAxHx@vHxAfQdKvrAnMjaBvBzTdAhJvBrPzCvRzArI~BvLfEpRXjArArFfCrJ~Ojj@|@pC`DfL~AnFDt@Lh@T|AD~@Ez@O~@Sh@[b@m@^e@Dg@Ga@Y]g@Si@]eAEg@Cm@@a@Lw@dEmI|@aBnCkF`A}ATa@h@eArB{DnCuFb@qAfKaSTY|AuCzCeGnDeHpNqXxQo]h[{l@`i@ccA|Ra_@LKlA_CTa@f@x@jB~EpGrP~@vCb@EvGaCx@OJILMrDcGbBsC~BsDnB_DlAsA`@]nAw@fCsA|DkAxCe@")
    val defaultSpeed = KilometersPerHour(120)
    val dummySubscriber = Subscriber( id = 1, imsi = "imsi", msisdn = "msisdn", imei = "imei", lastName = "lastName", firstName = "firstName", address = "address", city = "city", zip = "zip", country = "country" )
    def defaultTrip = Trip(defaultMcc, defaultMnc, dummySubscriber, defaultRoute, defaultSpeed, defaultSlide)

    "A TripHandler" should {

        "emit celltower messages along the route" in {

            val setup = new TestSetUp(app.actorSystem, "celltower-topic")
            val trip = defaultTrip
            setup.tripHandler ! trip
            Thread.sleep(2000)
            setup.tripHandler ! PoisonPill

            val events = setup.gimme().flatMap(_.asOpt[CelltowerEvent])
            logger.debug(events.toString)

            // we can't test for the exact celltower, since the database may change.
            // so we test the approximate distance

            val dist = events.last.celltower.location.distanceFrom(LatLng(51.045604, 3.725985))
            logger.debug("distance from reference: {}", dist.toString)
            assert (dist < 500.0) // less than 500 meters

        }

        "emit subscriber messages along the route" in {

            val setup = new TestSetUp(app.actorSystem, "subscriber-topic")
            val trip = Trip(defaultMcc, defaultMnc, dummySubscriber, defaultRoute, MetersPerSecond(10000), Seconds(1))
            setup.tripHandler ! trip
            Thread.sleep(2100)
            setup.tripHandler ! PoisonPill

            val events = setup.gimme().flatMap(_.asOpt[SubscriberEvent])
            logger.debug(events.toString)

            assert (events.length >= 2)

            val dist = events.last.location.distanceFrom(trip.route.from)
            logger.debug("distance from start: {}", dist.toString)

            assert (dist >= 20000)

        }

        "allow dynamic speed update" in {

            val setup = new TestSetUp(app.actorSystem, "subscriber-topic")
            val trip = defaultTrip
            val update = RequestUpdateEvent(None, Some(120000.0))
            setup.tripHandler ! trip
            setup.tripHandler ! update
            Thread.sleep(2000)
            setup.tripHandler ! PoisonPill

            val events = setup.gimme().flatMap(_.asOpt[SubscriberEvent])

            val distance = trip.route.from.distanceFrom(events.last.location)
            logger.info("distance covered: {}", distance.toString)

            // this is a bit tricky, because we cannot know when the change becomes effective
            // so we take a large margin
            assert (distance >= 50000.0)

        }

        "allow dynamic slide update" in {

            val setup = new TestSetUp(app.actorSystem, "subscriber-topic")
            val trip = defaultTrip
            val update = RequestUpdateEvent(Some(125), None)
            setup.tripHandler ! trip
            setup.tripHandler ! update
            Thread.sleep(2000)
            setup.tripHandler ! PoisonPill

            val events = setup.gimme().flatMap(_.asOpt[SubscriberEvent])
            logger.info("number of events: {}", events.length.toString)
            assert (events.length >= 13)

        }

    }

    /*
    helper stuff
     */

    val appConfig = Map(
        "testBroker.class" -> "traffic.brokers.MediatorBroker",
        "messageBrokers" -> List("testBroker"),
        "db.default.url" -> "jdbc:sqlite:dist/data/traffic.db",
        "play.akka.actor-system" -> "TripHandlerSpecSystem",
        "akka.remote.netty.tcp.hostname" -> "127.0.0.1",
        "akka.remote.netty.tcp.port" -> 0
    )

    implicit override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = appConfig)

    class MessageCollector() extends Actor {
        var events = List.empty[String]
        override def receive: Receive = {
            case s: String => events = events :+ s
            case Gimme => sender ! events
        }
    }

    case object Gimme

    class TestSetUp(system: ActorSystem, topic: String) {
        val collector = system.actorOf(Props(new MessageCollector))
        val tripHandler = system.actorOf(TripHandler.props())

        val mediator = DistributedPubSub(system).mediator
        mediator ! Subscribe(topic, collector)

        def gimme() = {
            implicit val timeout = Timeout(50.millis)
            val f = collector ? Gimme
            val events = Await.result(f, timeout.duration).asInstanceOf[List[String]].map(Json.parse)
            // logger.debug(events.toString)
            events
        }

    }

}
