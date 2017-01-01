package kpi.twitter.analysis.analytics

import java.util.UUID
import java.util.concurrent.Future

import com.typesafe.config.Config
import org.apache.log4j.Logger
import org.apache.spark.sql._
import kpi.twitter.analysis.utils._
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.functions._
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import twitter4j.Status

import scala.util.Random

object AnalyzerJob {

  private val log = Logger.getLogger(getClass)

  private val appName = buildInfo("name")
  private val version = buildInfo("version")
  private val config = getOptions("job.conf")

  def loadSentiment140File(sentiment140FilePath: String, sparkSession: SparkSession): DataFrame = {
    val tweetsDF = sparkSession.read
      .format("com.databricks.spark.csv")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(sentiment140FilePath)
      .toDF("polarity", "id", "date", "query", "user", "status")

    // Drop the columns we are not interested in.
    tweetsDF.drop("id").drop("date").drop("query").drop("user")
  }

  def getBarebonesTweetText(tweetText: String): String = {
    //Remove URLs, RT, MT and other redundant chars / strings from the tweets.
    tweetText.toLowerCase()
      .replaceAll("\n", "")
      .replaceAll("rt\\s+", "")
      .replaceAll("\\s+@\\w+", "")
      .replaceAll("@\\w+", "")
      .replaceAll("\\s+#\\w+", "")
      .replaceAll("#\\w+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
  }

  /**
    * Normalize sentiment for visualization perspective.
    * We are normalizing sentiment as we need to be consistent with the polarity value with Core NLP and for visualization.
    *
    * @param sentiment polarity of the tweet
    * @return normalized to either -1, 0 or 1 based on tweet being negative, neutral and positive.
    */
  def normalizeMLlibSentiment(sentiment: Double) = {
    sentiment match {
      case x if x == 0 => -1 // negative
      case x if x == 2 => 0 // neutral
      case x if x == 4 => 1 // positive
      case _ => 0 // if cant figure the sentiment, term it as neutral
    }
  }

  def saveModelToHDFS(sparkSession: SparkSession, path: String, model: PipelineModel): PipelineModel = {
    sparkSession.sparkContext.parallelize(Seq(model), 1).saveAsObjectFile(path)
    model
  }

  def loadModelFromHDFS(sparkSession: SparkSession, path: String): PipelineModel = {
    sparkSession.sparkContext.objectFile[PipelineModel](path).first()
  }

  def isTweetInEnglish(status: Status): Boolean = {
    status.getLang == "en" && status.getUser.getLang == "en"
  }

  def job(sparkSession: SparkSession, config: Config): Unit = {
    val tPath = config.getString(trainingPath)
    val mPath = config.getString(modelPath)

    var model: Option[PipelineModel] = None

    try {
      model = Some(loadModelFromHDFS(sparkSession, mPath))
      log.info(s"Loaded model $mPath from memory")
    } catch {
      case e: Throwable =>
        log.warn(s"Failed to load model from $mPath")
    }

    if (model.isEmpty) {
      log.info(s"Model $mPath does not exist")

      log.info(s"Load training dataset from $tPath")
      val tweetsDF: DataFrame = loadSentiment140File(tPath, sparkSession)

      val clearTextUdf = udf {(text: String) => getBarebonesTweetText(text)}
      val toDoubleUdf = udf[Double, String]( _.toDouble)

      val updatedTweetsDF = tweetsDF
        .select("polarity", "status")
        .withColumn("status", clearTextUdf(tweetsDF("status")))
        .withColumn("polarity", toDoubleUdf(tweetsDF("polarity")))
        .withColumnRenamed("polarity", "label")

      val tokenizer = new Tokenizer()
        .setInputCol("status").setOutputCol("words")

      val hashingTF = new HashingTF()
        .setInputCol(tokenizer.getOutputCol).setOutputCol("features")

      val nb = new NaiveBayes()
      nb.setModelType("multinomial")
      nb.setSmoothing(1.0)

      val pipeline = new Pipeline().setStages(Array(tokenizer, hashingTF, nb))

      model = Some(pipeline.fit(updatedTweetsDF))

      log.info("Saving model")
      saveModelToHDFS(sparkSession, mPath, model.get)
      log.info(s"Trained model was saved to $modelPath")
    }

    import sparkSession.sqlContext.implicits._

    val streamingContext = new StreamingContext(sparkSession.sparkContext, Milliseconds(config.getInt(batchDurationMs)))

    // create kafka connections
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> config.getString(kafkaBootstrapServers),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> UUID.randomUUID().toString,
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val srcTopic = Array(config.getString(kafkaTweetsAllTopic))
    val destTopic = config.getString(kafkaTweetsPredictedSentimentTopic)

    val kafkaStream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](srcTopic, kafkaParams)
    )

    val kafkaProducer: Broadcast[KafkaProducerWrapper[String, String]] = sparkSession.sparkContext
      .broadcast(KafkaProducerWrapper[String, String](createKafkaProducer(config)))

    kafkaStream.foreachRDD { record =>

      val clearedTweetsDF = record.flatMap(consumerRecord => {
        val status = TweetSerDe.fromString(consumerRecord.value())
        val clearedText = status.getText

        if (isTweetInEnglish(status)) {
          Seq((clearedText, consumerRecord.value()))
        } else {
          Seq()
        }

      }).toDF("status", "tweet").cache()

      val predictedDF = model.get.transform(clearedTweetsDF)

      predictedDF.cache()
      predictedDF.show(10)
      predictedDF.select("tweet", "prediction")
        .rdd
        .foreachPartition(partitionOfRecords => {
          val tweetsMetadata: Stream[Future[RecordMetadata]] = partitionOfRecords
            .map(data => {
            val tweetStr = data.getString(0)
            val prediction = data.getDouble(1)
            val normalizedSentiment = normalizeMLlibSentiment(prediction)
            kafkaProducer.value.send(destTopic, normalizedSentiment.toString, tweetStr)
          }).toStream
          tweetsMetadata.foreach { metadata => metadata.get() }
        })
    }

    log.info("Start processing")

    streamingContext.start()
    streamingContext.awaitTermination()
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
