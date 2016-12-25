package kpi.twitter.analysis.tools.spark

import com.typesafe.config.Config
import kpi.twitter.analysis.consumer.TwitterConsumer

object ConsumerJob {

  def apply(config: Config) {
    val sparkSession = SparkUtil.retrieveSparkSession("Twitter consumer test mode")

    TwitterConsumer.job(sparkSession, config)
  }
}
