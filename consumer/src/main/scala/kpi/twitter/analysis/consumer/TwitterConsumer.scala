package kpi.twitter.analysis.consumer

import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.sql.SparkSession

object TwitterConsumer extends App {
//  val log = LogManager.getRootLogger

  override def main(args: Array[String]) {
    val sparkSession = SparkSession.builder
      .appName("Twitter streaming consumer")
      .getOrCreate()

    runJob(sparkSession)
//    sc.addFile(args(1))
//    val rdd = sc.textFile(SparkFiles.get("conf.a"))
  }

  def runJob(sparkSession: SparkSession) = {
    // read from twitter implementation
    var i = 0
//    while(i < 1000) {
//      log.warn("Job is running " + i)
//      i = i + 1
//      Thread.sleep(1000)
//    }
  }
}