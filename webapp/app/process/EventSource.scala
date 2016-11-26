package process

import java.util.{Collections, Properties}

import kpi.twitter.analysis.utils.DataSource
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer

sealed trait Sentiment

case class Positive() extends Sentiment

case class Neutral() extends Sentiment

case class Negative() extends Sentiment


/**
  *
  */
class KafkaEventSource(val consumerProperties: Properties, val topic: String, val time: Time) extends DataSource[String] {
  private val kafkaConsumer = {
    val consumer = new KafkaConsumer[String, String](
      consumerProperties,
      new StringDeserializer,
      new StringDeserializer)
    consumer.subscribe(Collections.singletonList(topic))
    consumer
  }

  private var recordsIterator: Option[java.util.Iterator[ConsumerRecord[String, String]]] = None

  override def poll(timeout: Long, maxRecords: Long): Option[Seq[String]] = {
    val endTime = time.currentMillis + timeout
    var readSize: Long = 0

    var remainedTime: Long = 0
    var result = Seq[String]()

    // bound by timeout or record size
    while (readSize < maxRecords && {
      remainedTime = endTime - time.currentMillis
      remainedTime > 0
    }) {

      if (recordsIterator.isEmpty || !recordsIterator.get.hasNext) {
        recordsIterator = Some(kafkaConsumer.poll(remainedTime).iterator())
      }

      while (recordsIterator.get.hasNext && readSize < maxRecords) {
        result = result :+ recordsIterator.get.next().value()
        readSize += 1
      }
    }

    if (result.isEmpty) {
      None
    } else {
      Some(result)
    }
  }
}
