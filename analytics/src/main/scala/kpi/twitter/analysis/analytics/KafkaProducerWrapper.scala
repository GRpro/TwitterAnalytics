package kpi.twitter.analysis.analytics

import java.util.concurrent.Future

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

/**
  * Wrapper for [[KafkaProducer]] object
 *
  * @param createProducer function that returns [[KafkaProducer]] instance
  * @tparam K key type
  * @tparam V value type
  */
class KafkaProducerWrapper[K, V](createProducer: () => KafkaProducer[K, V]) extends Serializable {

  /* This is the key idea that allows us to work around running into
     NotSerializableExceptions. */
  lazy val producer = createProducer()

  def send(topic: String, key: K, value: V): Future[RecordMetadata] =
    producer.send(new ProducerRecord[K, V](topic, key, value))

  def send(topic: String, value: V): Future[RecordMetadata] =
    producer.send(new ProducerRecord[K, V](topic, value))

}

object KafkaProducerWrapper {

  def apply[K, V](createProducer: => KafkaProducer[K, V]): KafkaProducerWrapper[K, V] = {
    val createProducerFunc = () => {
      val producer = createProducer

      sys.addShutdownHook {
        // Ensure that, on executor JVM shutdown, the Kafka producer sends
        // any buffered messages to Kafka before shutting down.
        producer.close()
      }

      producer
    }
    new KafkaProducerWrapper(createProducerFunc)
  }

}