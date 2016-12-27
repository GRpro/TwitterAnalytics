package kpi.twitter.analysis.tools.spark

import java.util.Scanner

import com.typesafe.config.Config
import kpi.twitter.analysis.analytics.TwitterConsumerJob
import kpi.twitter.analysis.utils._

object ConsumerJob {

  def apply(config: Config) {
    val sparkSession = SparkUtil.retrieveSparkSession("Twitter consumer test mode")

    TwitterConsumerJob.job(sparkSession, config)
  }

  def main(args: Array[String]) {
    val config = getOptions("integration.conf")

    ConsumerJob(config)

    val sc = new Scanner(System.in)

    val stopCmd = "bye"
    while (!stopCmd.equals(sc.nextLine())) {
      println(s"use $stopCmd to stop Kafka server")
    }
  }
}
