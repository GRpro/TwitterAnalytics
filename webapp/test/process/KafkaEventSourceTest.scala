package process

import java.util
import java.util.Collections

import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer


class KafkaEventSourceTest extends FunSuite with MockitoSugar {


  test("subscribe should be invoked once for correct topic") {
    val topicName = "fake"
    val mockConsumer = mock[KafkaConsumer[String, String]]
    val mockTime = new MockTime

    val kafkaEventSource = new KafkaEventSource(mockConsumer, topicName, mockTime)
    verify(mockConsumer, times(1)).subscribe(Collections.singletonList(topicName))
  }

  /**
    * Test bound by time
    */
  test("poll should return on timeout") {

    val topicName = "fake"
    val mockConsumer = mock[KafkaConsumer[String, String]]
    val mockTime = new MockTime

    when(mockConsumer.poll(1000)).thenAnswer(new Answer[ConsumerRecords[String, String]]() {
      override def answer(invocation: InvocationOnMock): ConsumerRecords[String, String] = {
        val args = invocation.getArguments
        mockTime.sleep(args(0).asInstanceOf[Long])
        ConsumerRecords.empty[String, String]()
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
    val mockConsumer = mock[KafkaConsumer[String, String]]
    val mockTime = new MockTime

    when(mockConsumer.poll(1000)).thenAnswer(new Answer[ConsumerRecords[String, String]]() {
      override def answer(invocation: InvocationOnMock): ConsumerRecords[String, String] = {
        mockTime.sleep(1)
        val tp = new TopicPartition(topicName, 1)
        val record = new ConsumerRecord[String, String](topicName, 0, 0, "key", "value")
        val recordsMap = new util.HashMap[TopicPartition, util.List[ConsumerRecord[String, String]]]()
        val recordsList = new util.ArrayList[ConsumerRecord[String, String]]()
        recordsList.add(record)
        recordsMap.put(tp, recordsList)
        new ConsumerRecords[String, String](recordsMap)

      }
    })

    val kafkaEventSource = new KafkaEventSource(mockConsumer, topicName, mockTime)

    val records = kafkaEventSource.poll(1000, 1)

    assert(1 === records.size)
    assert(1 === mockTime.currentMillis)
  }
}
