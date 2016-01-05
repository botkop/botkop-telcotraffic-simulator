package traffic.brokers

import java.util.Properties

import org.apache.kafka.clients.producer.{ProducerRecord, KafkaProducer}

class KafkaBroker extends MessageBroker {

    val props = new Properties()

    props.put("metadata.broker.list", "broker1:9092,broker2:9092")
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    val producer = new KafkaProducer[String, String](props)

    override def send(message: String): Unit = producer.send(new ProducerRecord[String, String](null, message))
}
