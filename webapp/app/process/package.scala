import java.util
import java.util.Properties

import kpi.twitter.analysis.utils.TweetSerDe
import org.apache.kafka.clients.consumer.{Consumer, KafkaConsumer}
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import twitter4j.Status

package object process {

  class TwitterStatusDeserializer extends Deserializer[Status] {
    val stringDeserializer = new StringDeserializer()

    override def deserialize(topic: String, data: Array[Byte]): Status = {
      TweetSerDe.fromString(stringDeserializer.deserialize(topic, data))
    }

    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit =
      stringDeserializer.configure(configs, isKey)

    override def close(): Unit =
      stringDeserializer.close()
  }

  def createKafkaConsumer(consumerProperties: Properties): Consumer[String, Status] = {
    val consumer = new KafkaConsumer[String, Status](
      consumerProperties,
      new StringDeserializer,
      new TwitterStatusDeserializer)
    consumer
  }

  sealed trait Sentiment

  case class Positive() extends Sentiment

  case class Neutral() extends Sentiment

  case class Negative() extends Sentiment

}
