package process

import java.util.{Collections, Properties}

import kpi.twitter.analysis.utils.{DataSource, PredictedStatus}
import org.apache.kafka.clients.consumer.{Consumer, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import twitter4j.Status
import scala.util.Random;


/**
  * Kafka consumer that reads tweets from a specific topic
  */
class KafkaEventSource(createConsumer: => Consumer[String, Status], val topic: String, val time: Time) extends DataSource[PredictedStatus] {
  private val kafkaConsumer = createConsumer

  kafkaConsumer.subscribe(Collections.singletonList(topic))

  private var recordsIterator: Option[java.util.Iterator[ConsumerRecord[String, Status]]] = None

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
        result = result :+ PredictedStatus(Random.shuffle(List(-1, 0, 1)).head, recordsIterator.get.next().value())
        readSize += 1
      }
    }
    result
  }
}

object KafkaEventSource {

  def apply(consumerProperties: Properties, topic: String): KafkaEventSource =
    new KafkaEventSource(createKafkaConsumer(consumerProperties), topic, Time.default)

}