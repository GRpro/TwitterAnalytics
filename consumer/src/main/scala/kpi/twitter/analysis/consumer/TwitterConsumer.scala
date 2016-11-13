package kpi.twitter.analysis.consumer

import java.util.concurrent.Future

import com.google.gson.Gson
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import kpi.twitter.analysis.utils._
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

object TwitterConsumer {

  private val log = Logger.getLogger(getClass)

  private val appName = buildInfo("name")
  private val version = buildInfo("version")
  private val config = getOptions()

  private val gson = new Gson()



  /* Entry point to Spark streaming application */
  def main(args: Array[String]) {
    val sparkSession = SparkSession.builder
      .appName(s"$appName-$version")
      .getOrCreate()

    log.info("Version " + version)

    val streamingContext =
      new StreamingContext(
        sparkSession.sparkContext, Milliseconds(config.getInt(batchDurationMs)))

    System.setProperty("twitter4j.oauth.consumerKey", config.getString(twitterConsumerKey))
    System.setProperty("twitter4j.oauth.consumerSecret", config.getString(twitterConsumerSecret))
    System.setProperty("twitter4j.oauth.accessToken", config.getString(twitterAccessToken))
    System.setProperty("twitter4j.oauth.accessTokenSecret", config.getString(twitterAccessTokenSecret))

    val tweetStream = TwitterUtils
      .createStream(streamingContext, None, filters(config.getString(hashTagsFilter)))
      .map(gson.toJson(_))

    val topic = config.getString(kafkaTopic)
    import org.apache.kafka.common.serialization._

    val kafkaProducer: Broadcast[Producer[String, String]] = {
      val kafkaProducerConfig = {
        val p = new java.util.Properties()
        p.setProperty("bootstrap.servers", config.getString(kafkaBootstrapServers))
        p.setProperty("key.serializer", classOf[StringSerializer].getName)
        p.setProperty("value.serializer", classOf[StringSerializer].getName)
        p
      }
      sparkSession.sparkContext.broadcast(Producer[String, String](kafkaProducerConfig))
    }

    log.info("Start processing")
    jobPipeline(tweetStream, kafkaProducer.value, topic)

    streamingContext.start()
    streamingContext.awaitTermination()
  }

  def jobPipeline(tweetStream: DStream[String],
                  kafkaProducer: Producer[String, String],
                  topic: String) = {

    tweetStream.foreachRDD { (rdd, time) =>
      rdd.foreachPartition { partitionOfRecords =>
        val metadata: Stream[Future[RecordMetadata]] = partitionOfRecords.map { record =>
          kafkaProducer.send(topic, gson.toJson(record))
        }.toStream
        metadata.foreach { metadata => metadata.get() }
      }
    }
  }

  def filters(csString: String): Seq[String] = {
    if (csString.isEmpty) Nil
    else {
      csString.replaceAll("^[,\\s]+", "").split("[,\\s]+").toSeq
    }
  }
}