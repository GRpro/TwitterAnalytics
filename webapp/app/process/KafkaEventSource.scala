package process

import java.util
import java.util.{Collections, Properties}

import kpi.twitter.analysis.utils.{DataSource, PredictedStatus, SentimentLabel, TweetSerDe}
import org.apache.kafka.clients.consumer.{Consumer, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import twitter4j.Status

/**
  * Kafka consumer that reads tweets from a specific topic
  */
class KafkaEventSource(createConsumer: => Consumer[SentimentLabel, Status], val topic: String, val time: Time) extends DataSource[PredictedStatus] {
  private val kafkaConsumer = createConsumer

  kafkaConsumer.subscribe(Collections.singletonList(topic))

  private var recordsIterator: Option[java.util.Iterator[ConsumerRecord[SentimentLabel, Status]]] = None

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
        val sentimentLabel = record.key()
        val status = record.value()

        result = result :+ PredictedStatus(sentimentLabel.sentiment, sentimentLabel.probability, status)
        readSize += 1
      }
    }
    result
  }
}

object KafkaEventSource {

  class StatusDeserialized extends Deserializer[Status] {
    private val stringDeserializer = new StringDeserializer()
    override def configure(map: util.Map[String, _], b: Boolean): Unit = stringDeserializer.configure(map, b)
    override def close(): Unit = stringDeserializer.close()
    override def deserialize(s: String, bytes: Array[Byte]): Status =
      TweetSerDe.fromString(stringDeserializer.deserialize(s, bytes))
  }

  class LabelDeserializer extends Deserializer[SentimentLabel] {
    private val stringDeserializer = new StringDeserializer()
    override def configure(map: util.Map[String, _], b: Boolean): Unit = stringDeserializer.configure(map, b)
    override def close(): Unit = stringDeserializer.close()
    override def deserialize(s: String, bytes: Array[Byte]):
    SentimentLabel = SentimentLabel.deserialize(stringDeserializer.deserialize(s, bytes))
  }

  private def createKafkaConsumer(consumerProperties: Properties): Consumer[SentimentLabel, Status] = {
    val consumer = new KafkaConsumer[SentimentLabel, Status](
      consumerProperties,
      new LabelDeserializer,
      new StatusDeserialized)
    consumer
  }
  def apply(consumerProperties: Properties, topic: String): KafkaEventSource =
    new KafkaEventSource(createKafkaConsumer(consumerProperties), topic, Time.default)

}