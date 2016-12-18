package controllers

import play.api.mvc.{Action, Controller}
import kpi.twitter.analysis.utils._
import org.apache.kafka.clients.consumer.ConsumerConfig
import play.api.Logger
import play.api.libs.json.Json
import process.KafkaEventSource
import process._

class ClassifiedTweetsController extends Controller {

  lazy val options = getOptions()
  lazy val topic = options.getString(kafkaTopic)
  lazy val consumerConfig = {
    val props = new java.util.Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getString(kafkaBootstrapServers))
    props
  }

  lazy val kafkaConsumer = KafkaEventSource(consumerConfig, topic)

  def classifiedTweets() = Action {
    Logger.info("Read tweets")
    val tweets = kafkaConsumer.poll(1000, 1000)
    Ok(Json.toJson(tweets))
  }
}
