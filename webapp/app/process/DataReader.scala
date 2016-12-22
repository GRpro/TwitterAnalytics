package process

import java.util.UUID

import kpi.twitter.analysis.utils._
import org.apache.kafka.clients.consumer.ConsumerConfig
import play.api.Logger
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel

class DataReader(var channel: Concurrent.Channel[String]) extends Thread {

  setName("Data reader thread")

  // Kafka data source configuration
  lazy val options = getOptions()
  lazy val topic = options.getString(kafkaTopic)
  lazy val consumerConfig = {
    val props = new java.util.Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getString(kafkaBootstrapServers))
    props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString)
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props
  }

  override def run(): Unit = {
    try {
      val kafkaConsumer = KafkaEventSource(consumerConfig, topic)
      Logger.info("Consumer created")

      while (!isInterrupted) {

        val tweets = kafkaConsumer.poll(1000, 1000)
        Logger.info(s"Read ${tweets.length} records")

        tweets.foreach(tweet => channel.push(tweet))
      }

    } catch {
      case e: Throwable =>
        Logger.error("Exception while consuming tweets", e)
        interrupt()
    }
  }

}

object DataReader {
  def apply(channel: Channel[String]): DataReader = new DataReader(channel)
}
