package kpi.twitter.analysis.consumer

import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext
import org.scalatest.FunSuite

class TwitterConsumerIntegrationTest extends FunSuite {

  trait Config {
    val sparkSession = SparkSession.builder()
      .appName("Test Twitter Consumer")
      .master("local[*]")
      .getOrCreate()
  }

  // to be soon
  test("test no errors") {
//    new Config {
      //val job: StreamingContext = TwitterConsumer.jobPipeline(ArrasparkSession)
      //job.start()
      //job.awaitTermination()
//    }
  }

}
