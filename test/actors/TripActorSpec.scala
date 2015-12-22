package actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.scalalogging.{LazyLogging, Seq}
import model.{Route, Subscriber, Trip}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.test.WithApplication
import squants.space.Meters
import squants.{Length, Velocity}
import squants.motion.KilometersPerHour
import squants.time.Milliseconds

import scala.concurrent.duration._

class TripActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with LazyLogging {

    import TripActor._

    def makeTrip(): Trip = {
        val route = Route("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
        val sub = Subscriber.random().head
        val velocity: Velocity = KilometersPerHour(1200)
        Trip(sub, route, velocity)
    }

    def this() = this(ActorSystem("TripActorSpec"))

    /*
    override def afterAll() {
        TestKit.shutdownActorSystem(system)
    }
    */

    "A TripActor" must {

        "respond with locations along the route" in new WithApplication() {

            val trip = makeTrip()
            val tripActor = system.actorOf(TripActor.props(trip, Milliseconds(500), self))
            tripActor ! StartTrip

            val seq: Seq[TripProgress] = receiveN(6, 3.seconds).asInstanceOf[Seq[TripProgress]]
            logger.info(seq.toString())

            val distance = seq(5).distance.to(Meters)

            distance should be (833.3 +- 0.09)

        }
    }


}
