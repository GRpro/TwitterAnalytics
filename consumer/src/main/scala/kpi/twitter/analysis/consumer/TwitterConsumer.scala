package kpi.twitter.analysis.consumer

import java.util.concurrent.Future

import com.google.gson.Gson
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import kpi.twitter.analysis.utils._
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object TwitterConsumer {

  val log = Logger.getLogger(getClass)
  val appName = buildInfo("name")
  val version = buildInfo("version")

  def main(args: Array[String]) {
    val sparkSession = SparkSession.builder
      .appName(s"$appName-$version")
      .getOrCreate()

    log.info("Version " + version)

    val streamingContext = runJob(sparkSession)

    streamingContext.start()
    streamingContext.awaitTermination()
  }

  def runJob(sparkSession: SparkSession): StreamingContext = {
    val config = getOptions()
    val streamingContext = new StreamingContext(sparkSession.sparkContext,
      Seconds(config.getInt(batchDurationMs)))

    System.setProperty("twitter4j.oauth.consumerKey", config.getString(twitterConsumerKey))
    System.setProperty("twitter4j.oauth.consumerSecret", config.getString(twitterConsumerSecret))
    System.setProperty("twitter4j.oauth.accessToken", config.getString(twitterAccessToken))
    System.setProperty("twitter4j.oauth.accessTokenSecret", config.getString(twitterAccessTokenSecret))

    val tweetStream = TwitterUtils.createStream(streamingContext, None, filters(config.getString(hashTagsFilter)))

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

    val topic = config.getString(kafkaTopic)

    log.info("Start processing")
    tweetStream.foreachRDD { rdd =>
      rdd.saveAsTextFile("/tmp")
      rdd.foreachPartition { partitionOfRecords =>
        val metadata: Stream[Future[RecordMetadata]] = partitionOfRecords.map { record =>
          kafkaProducer.value.send(topic, new Gson().toJson(record))
        }.toStream
        metadata.foreach { metadata => metadata.get() }
      }
    }

    streamingContext
  }

  def filters(csString: String): Seq[String] = {
    if (csString.isEmpty) Nil
    else {
      csString.replaceAll("^[,\\s]+", "").split("[,\\s]+").toSeq
    }
  }
}