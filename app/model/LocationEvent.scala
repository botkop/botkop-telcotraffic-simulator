package model

import java.util.UUID

case class LocationEvent(
                        bearerId: String,
                        celltower: Celltower,
                        metrics: Map[String, Double]
                        )

object LocationEvent {

    def apply (celltower: Celltower): LocationEvent = {
        val bearerId = UUID.randomUUID().toString
        LocationEvent(bearerId, celltower)
    }

    def apply (bearerId: String, celltower: Celltower): LocationEvent = {
        val metrics = Map(
            "rtt" -> Math.random(),
            "jitter" -> Math.random()
        )
        LocationEvent(bearerId, celltower, metrics)
    }

}
