package traffic.brokers

import com.typesafe.scalalogging.LazyLogging

class LogBroker extends MessageBroker with LazyLogging {
    override def send(message: String): Unit = logger.info(message)
}
