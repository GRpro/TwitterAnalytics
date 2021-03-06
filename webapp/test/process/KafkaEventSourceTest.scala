package process

import java.util
import java.util.Collections

import kpi.twitter.analysis.utils.{PredictedStatus, SentimentLabel, TweetSerDe}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import twitter4j.Status


class KafkaEventSourceTest extends FunSuite with MockitoSugar {


  test("subscribe should be invoked once for correct topic") {
    val topicName = "fake"
    val mockConsumer = mock[KafkaConsumer[SentimentLabel, Status]]
    val mockTime = new MockTime

    val kafkaEventSource = new KafkaEventSource(mockConsumer, topicName, mockTime)
    verify(mockConsumer, times(1)).subscribe(Collections.singletonList(topicName))
  }

  /**
    * Test bound by time
    */
  test("poll should return on timeout") {

    val topicName = "fake"
    val mockConsumer = mock[KafkaConsumer[SentimentLabel, Status]]
    val mockTime = new MockTime

    when(mockConsumer.poll(1000)).thenAnswer(new Answer[ConsumerRecords[SentimentLabel, Status]]() {
      override def answer(invocation: InvocationOnMock): ConsumerRecords[SentimentLabel, Status] = {
        val args = invocation.getArguments
        mockTime.sleep(args(0).asInstanceOf[Long])
        ConsumerRecords.empty[SentimentLabel, Status]()
      }
    })

    val kafkaEventSource = new KafkaEventSource(mockConsumer, topicName, mockTime)

    val records = kafkaEventSource.poll(1000, 1)

    assert(0 === records.size)
    assert(1000 === mockTime.currentMillis)
  }

  /**
    * Test bound by records size
    */
  test("poll should return on max records") {

    val topicName = "fake"
    val mockConsumer = mock[KafkaConsumer[SentimentLabel, Status]]
    val mockTime = new MockTime

    when(mockConsumer.poll(1000)).thenAnswer(new Answer[ConsumerRecords[SentimentLabel, Status]]() {
      override def answer(invocation: InvocationOnMock): ConsumerRecords[SentimentLabel, Status] = {
        mockTime.sleep(1)
        val tp = new TopicPartition(topicName, 1)
        val record = new ConsumerRecord[SentimentLabel, Status](topicName, 0, 0, mock[SentimentLabel], mock[Status])
        val recordsMap = new util.HashMap[TopicPartition, util.List[ConsumerRecord[SentimentLabel, Status]]]()
        val recordsList = new util.ArrayList[ConsumerRecord[SentimentLabel, Status]]()
        recordsList.add(record)
        recordsMap.put(tp, recordsList)
        new ConsumerRecords[SentimentLabel, Status](recordsMap)

      }
    })

    val kafkaEventSource = new KafkaEventSource(mockConsumer, topicName, mockTime)

    val records = kafkaEventSource.poll(1000, 1)

    assert(1 === records.size)
    assert(1 === mockTime.currentMillis)
  }
}
