package kpi.twitter.analysis.consumer

import java.util.concurrent.Future

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

class Producer[K, V](createProducer: () => KafkaProducer[K, V]) extends Serializable {

  /* This is the key idea that allows us to work around running into
     NotSerializableExceptions. */
  lazy val producer = createProducer()

  def send(topic: String, key: K, value: V): Future[RecordMetadata] =
    producer.send(new ProducerRecord[K, V](topic, key, value))

  def send(topic: String, value: V): Future[RecordMetadata] =
    producer.send(new ProducerRecord[K, V](topic, value))

}

object Producer {

  import scala.collection.JavaConversions._

  def apply[K, V](config: Map[String, Object]): Producer[K, V] = {
    val createProducerFunc = () => {
      val producer = new KafkaProducer[K, V](config)

      sys.addShutdownHook {
        // Ensure that, on executor JVM shutdown, the Kafka producer sends
        // any buffered messages to Kafka before shutting down.
        producer.close()
      }

      producer
    }
    new Producer(createProducerFunc)
  }

  def apply[K, V](config: java.util.Properties): Producer[K, V] = apply(config.toMap)

}