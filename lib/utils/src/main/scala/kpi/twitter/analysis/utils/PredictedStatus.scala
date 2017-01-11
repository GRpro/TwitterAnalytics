package kpi.twitter.analysis.utils

import com.google.gson.Gson
import twitter4j.Status

class SentimentLabel(val sentiment: Int, val probability: Double)

object SentimentLabel {
  private val gson = new Gson()

  def serialize(sentimentLabel: SentimentLabel): String = gson.toJson(sentimentLabel)
  def deserialize(sentimentLabel: String): SentimentLabel = gson.fromJson(sentimentLabel, classOf[SentimentLabel])
}

case class PredictedStatus(predictedSentiment: Int, predictedProbability: Double, status: Status)
  extends SentimentLabel(predictedSentiment, predictedProbability)
