package kpi.twitter.analysis.consumer

import kpi.twitter.analysis.consumer.TwitterConsumer
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite

class TestTwitterConsumer extends FunSuite {

  trait Config {
    val sparkSession = SparkSession.builder()
      .appName("Test Twitter Consumer")
      .master("local")
      .getOrCreate()
  }

  test("test no errors") {
    new Config {
      TwitterConsumer.runJob(sparkSession)
    }
  }

}
