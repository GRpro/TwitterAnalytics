package kpi.twitter.analysis.cosumer

import org.apache.spark.sql.SparkSession

object TwitterConsumer extends App {

  def runJob(sparkSession: SparkSession) = {
    // read from twitter implementation
  }
  override def main(args: Array[String]) {
    val sparkSession = SparkSession.builder
      .appName("Twitter streaming consumer")
      .getOrCreate()

    runJob(sparkSession)
//    val spark = SparkSession.
//    sc.addFile(args(1))
//    val rdd = sc.textFile(SparkFiles.get("conf.a"))
  }
}