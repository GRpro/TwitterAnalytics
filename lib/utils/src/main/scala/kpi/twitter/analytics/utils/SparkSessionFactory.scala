package kpi.twitter.analytics.utils

import org.apache.spark.sql.SparkSession

trait SparkSessionFactory {
  def create(appName: String): SparkSession
}

object SparkSessionFactory {
  def default(): SparkSessionFactory = new SparkSessionFactory {
    override def create(appName: String): SparkSession = {
      val sparkSession = SparkSession.builder
        .appName(appName)
        .getOrCreate()
      sparkSession
    }
  }
}
