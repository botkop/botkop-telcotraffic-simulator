package traffic.brokers

import com.typesafe.scalalogging.LazyLogging

class LogBroker extends MessageBroker with LazyLogging {
    override def send(topic: String, message: String): Unit = logger.info(s"$topic: $message")
}

