package kpi.twitter.analysis.tools

import java.util.Scanner

import kpi.twitter.analysis.analytics.AnalyzerJob
import kpi.twitter.analysis.tools.kafka.KafkaZookeeper
import kpi.twitter.analysis.tools.spark.{ConsumerJob, SparkUtil}
import kpi.twitter.analysis.utils._

/**
  * Environment for running application during development
  */
object Bootstrap {

  val config = getOptions("integration.conf")

  def main(args: Array[String]) {
    val kafkaPort = config.getInt(kafkaBrokerPort)
    val zookeeperPort = config.getInt(kafkaZookeeperPort)
    val allTweetsTopic = config.getString(kafkaTweetsAllTopic)
    val analyzedTweetsTopic = config.getString(kafkaTweetsPredictedSentimentTopic)

//    val kafkaZookeeper = KafkaZookeeper(kafkaPort, zookeeperPort)
//    kafkaZookeeper.start()
//    kafkaZookeeper.createTopic(allTweetsTopic, 3, 1)
//    kafkaZookeeper.createTopic(analyzedTweetsTopic, 3, 1)

    // Twitter consumer job
//    ConsumerJob(config)

    // Twitter analytics job
    AnalyzerJob.job(
      SparkUtil.retrieveSparkSession("Analyzer test app"), config)

    val sc = new Scanner(System.in)

    val stopCmd = "bye"
    while (!stopCmd.equals(sc.nextLine())) {
      println(s"use $stopCmd to stop Kafka server")
    }

//    kafkaZookeeper.stop()
  }

}
