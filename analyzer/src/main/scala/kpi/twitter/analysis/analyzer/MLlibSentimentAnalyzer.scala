package kpi.twitter.analysis.analyzer

import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

object MLlibSentimentAnalyzer {

  def computeSentiment(text: String, model: NaiveBayesModel): Int = {
    val tweetInWords: Seq[String] = getBarebonesTweetText(text)
    val polarity = model.predict(MLlibSentimentAnalyzer.transformFeatures(tweetInWords))
    normalizeMLlibSentiment(polarity)
  }

  def normalizeMLlibSentiment(sentiment: Double): Int = {
    sentiment match {
      case x if x == 0 => -1 // negative
      case x if x == 2 => 0 // neutral
      case x if x == 4 => 1 // positive
      case _ => 0 // if cant figure the sentiment, term it as neutral
    }
  }

  def getBarebonesTweetText(tweetText: String): Seq[String] = {
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
      .split("\\W+")
      .filter(_.matches("^[a-zA-Z]+$"))
  }

  val hashingTF = new HashingTF()

  def transformFeatures(tweetText: Seq[String]): Vector = {
    hashingTF.transform(tweetText)
  }
}