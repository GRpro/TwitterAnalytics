package process

import java.util.UUID

import kpi.twitter.analysis.utils._
import org.apache.kafka.clients.consumer.ConsumerConfig
import play.api.Logger
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel
import twitter4j.Status

class DataReader(var channel: Concurrent.Channel[String]) extends Thread {

  setName("Data reader thread")

  // Kafka data source configuration
  lazy val options = getOptions("environment.conf")
  lazy val topic = options.getString(kafkaTweetsPredictedSentimentTopic)
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

        val tweets = kafkaConsumer.poll(1000, 10)
        Logger.info(s"Read ${tweets.length} records")

        // display on map tweets with defined geolocation
        // for other implement ML processing which is TODO
        tweets
          //.filter(hasGeoLocation(_))
            .filter(t => isTweetInEnglish(t.status))
          .foreach(status => {
            channel.push(TweetSerDe.toString(status))
          })

        Thread.sleep(5000)
      }

    } catch {
      case e: Throwable =>
        Logger.error("Exception while consuming tweets", e)
        interrupt()
    }
  }

  def hasGeoLocation(status: Status): Boolean = {
    null != status.getGeoLocation
  }

  def isTweetInEnglish(status: Status): Boolean = {
    status.getLang == "en" && status.getUser.getLang == "en"
  }

}

object DataReader {
  def apply(channel: Channel[String]): DataReader = new DataReader(channel)
}
