package traffic.brokers

import java.util

import play.api.Configuration
import play.api.Play.current
import play.api.libs.json.Json
import traffic.protocol.{SubscriberEvent, AttachEvent, CelltowerEvent, TopicEvent}

import scala.collection.JavaConversions._

object ConfiguredBrokers {

    private val conf = current.configuration

    val brokers = conf.getStringList("messageBrokers")
        .getOrElse(new util.ArrayList[String]())
        .map { brokerName: String =>

            val brokerConfig: Configuration = conf.getConfig(brokerName).get
            val clazzName = brokerConfig.getString("class").get

            val broker = Class.forName(clazzName).newInstance.asInstanceOf[MessageBroker]

            brokerConfig.getConfig("properties") match {
                case Some(properties) =>
                    broker.configure(properties)
                case _ =>
            }

            broker
        }.toList

    def publish(topic: String, message: String): Unit = {
        brokers.foreach(_.send(topic, message))
    }

    def publish (topicMessage: TopicEvent): Unit = {
        topicMessage match {
            case msg: CelltowerEvent =>
                publish(msg.topic, Json.stringify(Json.toJson(msg)))
            case msg: AttachEvent =>
                publish(msg.topic, Json.stringify(Json.toJson(msg)))
            case msg: SubscriberEvent =>
                publish(msg.topic, Json.stringify(Json.toJson(msg)))
        }
    }

}
