package kpi.twitter.analysis.tools.kafka

import java.util.{Collections, Properties, UUID}

import org.apache.kafka.clients.consumer.{Consumer, ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer

object KafkaConsoleConsumer {

  def createConsumer(): Consumer[String, String] = {
    val consumerProperties = new Properties()
    consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094")
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString)
    consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

    val consumer = new KafkaConsumer[String, String](consumerProperties, new StringDeserializer, new StringDeserializer)

    consumer.subscribe(Collections.singletonList("predicted-sentiment-tweets"))
    consumer
  }

  def main(args: Array[String]) {
    val consumer = createConsumer()

    while (true) {
      val it = consumer.poll(1000).iterator()
//      if (!it.hasNext) {
//        println("empty :(")
//      }
      while (it.hasNext) {
        val record = it.next()
        println(s"${record.key()} : ${record.value()}")
      }
    }
  }
}
