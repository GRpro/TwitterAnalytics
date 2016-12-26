package kpi.twitter.analysis.analyzer

import kpi.twitter.analysis.utils._
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}

object NaiveBayesModelCreator {

  private val appName = buildInfo("name")
  private val version = buildInfo("version")
  private val config = getOptions()

  def main(args: Array[String]) {

    val sparkSession = SparkSession.builder
      .appName(s"$appName-$version")
      .getOrCreate()

    createAndSaveNBModel(sparkSession)
    sparkSession.stop()
  }

  def createAndSaveNBModel(sparkSession: SparkSession): Unit = {
    val tweetsDF: DataFrame = loadSentiment140File(config.getString(trainingSetPath), sparkSession)

    val labeledRDD = tweetsDF.select("polarity", "status").rdd.map {
      case Row(polarity: Int, tweet: String) =>
        val tweetInWords: Seq[String] = MLlibSentimentAnalyzer.getBarebonesTweetText(tweet)
        LabeledPoint(polarity, MLlibSentimentAnalyzer.transformFeatures(tweetInWords))
    }
    labeledRDD.cache()

    val naiveBayesModel: NaiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
    naiveBayesModel.save(sparkSession.sparkContext, config.getString(modelPath))
  }

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
}
