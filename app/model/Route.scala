package model

import com.typesafe.scalalogging.LazyLogging
import geo.{LatLng, Polyline}
import play.api.Play.current
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._



case class Route(polyline: Polyline) {
    def from: LatLng = polyline.path.head
    def to: LatLng = polyline.path.last
    def distance = polyline.distance()
    def position(distance: Double) = polyline.pointAtDistance(distance)
}

object Route extends LazyLogging {

    def byGoogle (from: LatLng, to: LatLng): Option[Route] = {

        val apiKey: String = current.configuration.getString("google.api.key").get

        val apiBase = "https://maps.googleapis.com/maps/api/directions/json?"
        val apiKeyStr = s"&key=$apiKey"
        val origin = s"origin=${from.lat},${from.lng}"
        val destination = s"&destination=${to.lat},${to.lng}"
        val apiCall = s"$apiBase$origin$destination$apiKeyStr"

        val holder  = WS.url(apiCall)

        val future: Future[String] = holder.get().map {
            response =>
                ((response.json \\ "overview_polyline").head \ "points").as[String]
        }

        Await.result(future, 3.seconds) match {
            case polyline: String =>
                Some(Route(polyline))
            case err =>
                logger.error(s"error retrieving directions from google: $err")
                None
        }
    }

    /* string based constructor */
    def apply (str: String): Route = {
        require(str.length > 0, "the polyline must be non-empty")
        val polyline = Polyline(str)
        Route(polyline)
    }

}

