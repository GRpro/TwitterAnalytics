package kpi.twitter.analysis

package object consumer {


  /* configuration keys */
  val twitterConsumerKey = "twitter.consumerKey"
  val twitterConsumerSecret = "twitter.consumerSecret"
  val twitterAccessToken = "twitter.accessToken"
  val twitterAccessTokenSecret = "twitter.accessTokenSecret"

  val batchDurationMs = "batch.duration.ms"

  // comma-separated list of hash-tags, e.g. #spark,#bigdata
  val hashTagsFilter = "filter.hashtags"
  val kafkaBootstrapServers = "kafka.bootstrap.servers"
  val kafkaTopic = "kafka.topic"
}
