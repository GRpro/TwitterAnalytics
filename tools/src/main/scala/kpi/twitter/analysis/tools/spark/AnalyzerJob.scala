package kpi.twitter.analysis.tools.spark

import java.util.Scanner

import com.typesafe.config.Config
import kpi.twitter.analysis.utils._

object AnalyzerJob {

  def apply(config: Config) {
    val sparkSession = SparkUtil.retrieveSparkSession("Twitter consumer test mode")

    kpi.twitter.analysis.analytics.AnalyzerJob.job(sparkSession, config)
  }

  def main(args: Array[String]) {
    val config = getOptions("integration.conf")

    AnalyzerJob(config)

    val sc = new Scanner(System.in)

    val stopCmd = "bye"
    while (!stopCmd.equals(sc.nextLine())) {
      println(s"use $stopCmd to stop Kafka server")
    }
  }
}
