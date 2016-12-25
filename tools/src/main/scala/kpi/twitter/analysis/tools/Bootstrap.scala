package kpi.twitter.analysis.tools

import java.util.Scanner

import kpi.twitter.analysis.tools.kafka.KafkaZookeeper
import kpi.twitter.analysis.tools.spark.ConsumerJob
import kpi.twitter.analysis.utils._

/**
  * Environment for running application during development
  */
object Bootstrap {

  val config = getOptions("integration.conf")

  def main(args: Array[String]) {
    val kafkaPort = config.getInt(kafkaBrokerPort)
    val zookeeperPort = config.getInt(kafkaZookeeperPort)
    val kafkaTopic = config.getString(kafkaTweetsAllTopic)

    val kafkaZookeeper = KafkaZookeeper(kafkaPort, zookeeperPort)
    kafkaZookeeper.start()
    kafkaZookeeper.createTopic(kafkaTopic, 3, 1)

    ConsumerJob(config)


    val sc = new Scanner(System.in)

    val stopCmd = "bye"
    while (!stopCmd.equals(sc.nextLine())) {
      println(s"use $stopCmd to stop Kafka server")
    }

    kafkaZookeeper.stop()
  }

}
