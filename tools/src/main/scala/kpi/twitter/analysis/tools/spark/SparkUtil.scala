package kpi.twitter.analysis.tools.spark

import org.apache.spark.sql.SparkSession

object SparkUtil {

  /**
    * Retrieve object of [[SparkSession]] to run jobs in local mode
    *
    * @param appName the name of Spark application
    * @return [[SparkSession]] object
    */
  def retrieveSparkSession(appName: String): SparkSession = {
    val sparkSession = SparkSession.builder.master("local[*]")
      .appName(appName)
      .getOrCreate()
    sparkSession
  }
}
