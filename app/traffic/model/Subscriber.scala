package traffic.model

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json.Json
import scala.language.postfixOps

case class Subscriber(
                         id: Int,
                         imsi: String,
                         msisdn: String,
                         imei: String,
                         lastName: String,
                         firstName: String,
                         address: String,
                         city: String,
                         zip: String,
                         country: String)


object Subscriber {

    implicit val requestWrites = Json.writes[Subscriber]
    implicit val requestReads = Json.reads[Subscriber]

    val subscriberRowParser: RowParser[Subscriber] = {
        int("phone_ids.id") ~
            str("phone_ids.imsi") ~
            str("phone_ids.msisdn") ~
            str("phone_ids.imei") ~
            str("identities.surname") ~
            str("identities.givenname") ~
            str("identities.streetaddress") ~
            str("identities.city") ~
            str("identities.zipcode") ~
            str("identities.countryfull") map {
            case id ~ imsi ~ msisdn ~ imei ~ surname ~ givenname ~ streetaddress ~ city ~ zip ~ country =>
                Subscriber(id, imsi, msisdn, imei, surname, givenname, streetaddress, city, zip, country)
        }
    }

    val subscriberParser: ResultSetParser[List[Subscriber]] = {
        subscriberRowParser *
    }

    def getOne(id: Int): Option[Subscriber] = DB.withConnection {
        val sql: SqlQuery = SQL(
            s"""
               |select *
               |from identities id, phone_ids pi
               |where id.id = $id
               |and id.id = pi.id
               |""".stripMargin)

        implicit connection =>
            val rs: List[Subscriber] = sql.as(subscriberParser)
            rs.headOption
    }

    def random(count: Int = 1): List[Subscriber] = DB.withConnection {
        val sql: SqlQuery = SQL(
            s"""
               |select *
               |from identities id, phone_ids pi
               |where id.id = pi.id
               |order by random()
               |limit $count
               |""".stripMargin)

        implicit connection =>
            sql.as(subscriberParser)
    }

}

