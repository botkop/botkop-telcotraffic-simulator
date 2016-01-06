package traffic.brokers

import com.typesafe.scalalogging.LazyLogging
import play.api.Configuration

class LogBroker extends MessageBroker with LazyLogging {
    override def send(topic: String, message: String): Unit = logger.info(s"$topic: $message")

    override def configure(config: Configuration): Unit = {
        // nothing to configure
    }
}
