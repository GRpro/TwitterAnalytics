package kpi.twitter.analysis

import com.typesafe.config.{Config, ConfigFactory}

package object utils {

  /**
    * kpi.twitter.analysis.BuildInfo class is auto generated before
    * compilation phase by all other modules that depend on this one.
    */
  lazy val buildInfo = Class.forName("kpi.twitter.analysis.BuildInfo")
    .newInstance
    .asInstanceOf[{ val info: Map[String, String] }]
    .info

  def getOptions(fileName: String = "application.conf"): Config =
    ConfigFactory.load(fileName)





  /*

  Project specific configuration keys for all project
  modules should be declared and explained here to
  enhance readability

  */

  // Twitter OAuth credentials
  val twitterConsumerKey = "twitter.consumerKey"
  val twitterConsumerSecret = "twitter.consumerSecret"
  val twitterAccessToken = "twitter.accessToken"
  val twitterAccessTokenSecret = "twitter.accessTokenSecret"

  /**
    * The time interval at which streaming data will be divided into batches
    */
  val batchDurationMs = "batch.duration.ms"

  /**
    * Comma-separated list of hash-tags, e.g.
    * @example #spark,#bigdata
    */
  val hashTagsFilter = "filter.hashtags"

  /**
    * Initial set of Kafka brokers to discover Kafka cluster from
    */
  val kafkaBootstrapServers = "kafka.bootstrap.servers"

  /**
    * Topic to store all read tweets
    */
  val kafkaTweetsAllTopic = "kafka.tweets.all.topic"

  val trainingPath = "training.path"

  val modelPath = "model.path"

  val kafkaTweetsPredictedSentimentTopic = "kafka.tweets.predicted.sentiment.topic"
}
