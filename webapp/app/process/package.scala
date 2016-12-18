import java.util.Properties

import org.apache.kafka.clients.consumer.{Consumer, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer

package object process {

  val kafkaBootstrapServers = "kafka.bootstrap.servers"
  val kafkaTopic = "kafka.topic"

  def createKafkaConsumer(consumerProperties: Properties): Consumer[String, String] = {
    val consumer = new KafkaConsumer[String, String](
      consumerProperties,
      new StringDeserializer,
      new StringDeserializer)
    consumer
  }

  sealed trait Sentiment

  case class Positive() extends Sentiment

  case class Neutral() extends Sentiment

  case class Negative() extends Sentiment

}
