package kpi.twitter.analysis.analytics

import java.util.concurrent.Future

import com.typesafe.config.Config
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.kafka.clients.producer.{KafkaProducer, RecordMetadata}
import org.apache.kafka.common.serialization._
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import twitter4j.Status

import kpi.twitter.analysis.utils._

/**
  * Spark Job that reads from public Twitter stream and publishes
  * retrieved tweets to Kafka for performing analytics
  */
object TwitterConsumerJob {

  private val log = Logger.getLogger(getClass)

  private val appName = buildInfo("name")
  private val version = buildInfo("version")
  private val config = getOptions("job.conf")


  def createTwitterStream(streamingContext: StreamingContext, config: Config): ReceiverInputDStream[Status] = {
    System.setProperty("twitter4j.oauth.consumerKey", config.getString(twitterConsumerKey))
    System.setProperty("twitter4j.oauth.consumerSecret", config.getString(twitterConsumerSecret))
    System.setProperty("twitter4j.oauth.accessToken", config.getString(twitterAccessToken))
    System.setProperty("twitter4j.oauth.accessTokenSecret", config.getString(twitterAccessTokenSecret))

    TwitterUtils.createStream(streamingContext, None, filters(config.getString(hashTagsFilter)))
  }

  def job(sparkSession: SparkSession, config: Config,
          createTwitterStream: (StreamingContext, Config) => ReceiverInputDStream[Status] = createTwitterStream,
          createKafkaProducer: (Config) => KafkaProducer[String, String] = createKafkaProducer): Unit = {

    val streamingContext = new StreamingContext(sparkSession.sparkContext, Milliseconds(config.getInt(batchDurationMs)))

    val tweetStream = createTwitterStream(streamingContext, config)

    val kafkaProducer: Broadcast[KafkaProducerWrapper[String, String]] = sparkSession.sparkContext
      .broadcast(KafkaProducerWrapper[String, String](createKafkaProducer(config)))

    val allTweetsTopic = config.getString(kafkaTweetsAllTopic)

    tweetStream.foreachRDD { (rdd, time) =>
      rdd.foreachPartition { partitionOfRecords =>
        val allTweetsMetadata: Stream[Future[RecordMetadata]] = partitionOfRecords.map { status =>
          // produce to topic with all tweets
          kafkaProducer.value.send(allTweetsTopic, TweetSerDe.toString(status))
        }.toStream
        allTweetsMetadata.foreach { metadata => metadata.get() }
      }
    }

    log.info("Start processing")

    streamingContext.start()
    streamingContext.awaitTermination()
  }

  def filters(csString: String): Seq[String] = {
    if (csString.isEmpty) Nil
    else {
      csString.replaceAll("^[,\\s]+", "").split("[,\\s]+").toSeq
    }
  }

  def main(args: Array[String]) {
    log.info(s"Version: $version")
    log.info(s"Configuration: $config")

    val sparkSession = SparkSession.builder
      .appName(s"$appName-$version")
      .getOrCreate()

    job(sparkSession, config)
  }

}
