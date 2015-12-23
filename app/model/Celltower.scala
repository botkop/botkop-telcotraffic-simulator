package model

import anorm.SqlParser._
import anorm._
import com.typesafe.scalalogging.LazyLogging
import geo.LatLng
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps

case class Celltower (mcc: Int, mnc: Int, cell: Int, area: Int, location: LatLng) {
    def toJson =
        s"""
           |{
           |  "mcc": $mcc,
           |  "mnc": $mnc,
           |  "cell": $cell,
           |  "area": $area,
           |  "location": ${location.toJson}
           |}
         """.stripMargin
}

object Celltower extends LazyLogging {

    val celltowerRowParser: RowParser[Celltower] = {
            int("cell_towers.mcc") ~
            int("cell_towers.net") ~
            int("cell_towers.cell") ~
            int("cell_towers.area") ~
            double("cell_towers.lat") ~
            double("cell_towers.lon") map {
                case mcc ~ mnc ~ cell ~ area ~ lat ~ lon =>
                    Celltower(mcc, mnc, cell, area, LatLng(lat, lon))
            }
    }

    val celltowerParser: ResultSetParser[List[Celltower]] = {
        celltowerRowParser *
    }

    def getAll(mcc: Int, mnc: Int, limit: Int = 0): List[Celltower] = DB.withConnection {
        val limitClause = if (limit == 0) "" else s"limit $limit"
        val sql: SqlQuery = SQL(s"""
           |select *
           |from cell_towers
           |where mcc = $mcc and net = $mnc
           |$limitClause
           |""".stripMargin)

        implicit connection =>
            sql.as(celltowerParser)
    }

    def getOne(mcc: Int, mnc: Int, cell: Int, area: Int): Option[Celltower] = DB.withConnection {
        val sql: SqlQuery = SQL(s"""
            |select *
            |from cell_towers
            |where mcc = $mcc and net = $mnc
            |and cell = $cell and area = $area
            |""".stripMargin)

        implicit connection =>
            val rs: List[Celltower] = sql.as(celltowerParser)
            rs.headOption
    }

    def getRandom(mcc: Int, mnc: Int, count: Int = 1): List[Celltower] = DB.withConnection {
        val sql: SqlQuery = SQL(s"""
                                   |select *
                                   |from cell_towers
                                   |where mcc = $mcc and net = $mnc
                                   |order by random()
                                   |limit $count
                                   |""".stripMargin)

        implicit connection =>
            sql.as(celltowerParser)
    }

    def getNearest(mcc: Int, mnc: Int, location: LatLng): Celltower = {
        val all = getAll(mcc, mnc)
        var minDist = Double.MaxValue
        var minCelltower: Celltower = null

        all.foreach {
            case ct: Celltower =>
                val dist = location.distanceFrom(ct.location)
                if (dist < minDist) {
                    minDist = dist
                    minCelltower = ct
                }
        }

        minCelltower
    }


}
