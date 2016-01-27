package traffic.brokers

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.Configuration

class KafkaBroker extends MessageBroker {

    var producer: KafkaProducer[String, String] = _

    override def send(topic: String, message: String): Unit = producer.send(new ProducerRecord[String, String](topic, message))

    override def configure(config: Configuration) = {
        val props = new Properties()
        config.keys.foreach { k =>
            props.put(k, config.getString(k).get)
        }
        producer = new KafkaProducer[String, String](props)
    }

}
