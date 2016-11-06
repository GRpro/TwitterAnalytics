package kpi.twitter.analysis.consumer

import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession

import kpi.twitter.analysis.utils._

object TwitterConsumer {

  val log = Logger.getLogger(getClass)
  val appName = buildInfo("name")
  val version = buildInfo("version")

  def main(args: Array[String]) {
    val sparkSession = SparkSession.builder
      .appName(s"$appName-$version")
      .getOrCreate()

    log.info("Version " + version)

    runJob(sparkSession)
  }

  def runJob(sparkSession: SparkSession) = {

  }
}