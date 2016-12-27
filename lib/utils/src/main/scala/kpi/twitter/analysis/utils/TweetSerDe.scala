package kpi.twitter.analysis.utils

import com.google.gson.Gson
import twitter4j.{Status, TwitterObjectFactory}

/**
  * Utility to standardize serialization/deserialization
  * of tweets during processing pipeline
  */
object TweetSerDe {
  private val gson = new Gson()

  def toString(status: Status): String = {
    gson.toJson(status)
  }

  def fromString(string: String): Status = {
    TwitterObjectFactory.createStatus(string)
  }

  def toString(status: PredictedStatus): String = {
    gson.toJson(status)
  }
}
