package process

import java.util.{Collections, Properties}

import kpi.twitter.analysis.utils.{DataSource, PredictedStatus, SentimentLabel, TweetSerDe}
import org.apache.kafka.clients.consumer.{Consumer, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import twitter4j.Status

import scala.util.Random;


/**
  * Kafka consumer that reads tweets from a specific topic
  */
class KafkaEventSource(createConsumer: => Consumer[String, String], val topic: String, val time: Time) extends DataSource[PredictedStatus] {
  private val kafkaConsumer = createConsumer

  kafkaConsumer.subscribe(Collections.singletonList(topic))

  private var recordsIterator: Option[java.util.Iterator[ConsumerRecord[String, String]]] = None

  override def poll(timeout: Long, maxRecords: Long): Seq[PredictedStatus] = {
    val endTime = time.currentMillis + timeout
    var readSize: Long = 0

    var remainedTime: Long = 0
    var result = Seq[PredictedStatus]()

    // bound by timeout or record size
    while (readSize < maxRecords && {
      remainedTime = endTime - time.currentMillis
      remainedTime > 0
    }) {

      if (recordsIterator.isEmpty || !recordsIterator.get.hasNext) {
        recordsIterator = Some(kafkaConsumer.poll(remainedTime).iterator())
      }

      while (recordsIterator.get.hasNext && readSize < maxRecords) {
        val record = recordsIterator.get.next()
        val sentimentLabel = SentimentLabel.deserialize(record.key())
        val status = TweetSerDe.fromString(record.value())

        result = result :+ PredictedStatus(sentimentLabel.sentiment, sentimentLabel.probability, status)
        readSize += 1
      }
    }
    result
  }
}

object KafkaEventSource {

  private def createKafkaConsumer(consumerProperties: Properties): Consumer[String, String] = {
    val consumer = new KafkaConsumer[String, String](
      consumerProperties,
      new StringDeserializer,
      new StringDeserializer)
    consumer
  }
  def apply(consumerProperties: Properties, topic: String): KafkaEventSource =
    new KafkaEventSource(createKafkaConsumer(consumerProperties), topic, Time.default)

}