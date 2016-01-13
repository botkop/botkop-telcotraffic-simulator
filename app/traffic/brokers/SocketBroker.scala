package traffic.brokers

import java.io._
import java.net.{InetAddress, Socket}
import java.util

import com.typesafe.scalalogging.LazyLogging
import play.api.Configuration

import scala.collection.JavaConversions._

// TODO: this broker does not implement Automatic Resource Management (ARM)
// so this could cause all sorts of unpleasantness
class SocketBroker extends MessageBroker with LazyLogging {

    var streamMap = scala.collection.mutable.Map[String, PrintStream]()

    override def configure(config: Configuration) = {

        val topics: util.List[Configuration] = config.getConfigList("topics").get

        topics.foreach { cfg: Configuration =>
            val topic = cfg.getString("topic").get
            val host = cfg.getString("host").get
            val port = cfg.getInt("port").get
            try {
                val socket = new Socket(InetAddress.getByName(host), port)
                val out = new PrintStream(socket.getOutputStream)
                streamMap += (topic -> out)
            } catch {
                case e: Exception => logger.error(s"unable to connect to $host:$port", e)
            }
        }
    }

    override def send(topic: String, message: String) = {
        streamMap.get(topic) match {
            case Some(ps) =>
                ps.println(message)
                ps.flush()
            case None =>
                // fall back to logging
                logger.info(s"$topic: $message")
        }
    }

}
