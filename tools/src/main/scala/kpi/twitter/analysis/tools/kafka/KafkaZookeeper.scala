package kpi.twitter.analysis.tools.kafka

import java.io.File
import java.util.{Properties, Scanner, UUID}

import scala.util.Random
import org.apache.commons.io.FileUtils
import org.apache.curator.test.TestingServer
import org.apache.log4j.Logger
import kafka.admin.AdminUtils
import kafka.server.{KafkaConfig, KafkaServerStartable}
import kafka.utils.ZkUtils
import kpi.twitter.analysis.tools._
import kpi.twitter.analysis.utils._

/**
  * Implementation on single-broker Kafka cluster
  */
class KafkaZookeeper(port: Int = 9092, zkPort: Int = 2181)(implicit val log: Logger = Logger.getLogger("kafka.EmbeddedKafka")) {
  private val zookeeper = new TestingServer(zkPort, false)
  private val zkUrl = zookeeper.getConnectString
  private val logDir = new File(System.getProperty("java.io.tmpdir"), s"embedded-kafka-logs/${UUID.randomUUID.toString}")
  private lazy val zkUtils = ZkUtils(zkUrl, 5000, 5000, isZkSecurityEnabled = false)

  private val props = new Properties()
  props.setProperty("zookeeper.connect", zkUrl)
  props.setProperty("reserved.broker.max.id", "1000000")
  props.setProperty("broker.id", Random.nextInt(1000000).toString)
  props.setProperty("port", s"$port")
  props.setProperty("log.dirs", logDir.getAbsolutePath)
  props.setProperty("delete.topic.enable", "true")
  props.setProperty("auto.create.topics.enable", "false")
  props.setProperty("advertised.host.name", "localhost")
  props.setProperty("advertised.port", port.toString)
  private val kafka = new KafkaServerStartable(new KafkaConfig(props))

  def createTopic(topic: String, partitions: Int = 1, replicationFactor: Int = 1) = {
    AdminUtils.createTopic(zkUtils, topic, partitions, replicationFactor, new Properties)
    while(!topicExists(topic)) Thread.sleep(200)
    log.info(s"Created topic: $topic")
  }

  def createTopics(topics: String*) = topics.foreach(t => createTopic(t))

  def deleteTopic(topic: String) = {
    AdminUtils.deleteTopic(zkUtils, topic)
    while(topicExists(topic)) Thread.sleep(200)
    log.info(s"Deleted topic: $topic")
  }

  def deleteTopics(topics: String*) = topics.foreach(t => deleteTopic(t))

  def topicExists(topic: String) = AdminUtils.topicExists(zkUtils, topic)

  def start() = {
    log.info("Starting Kafka..")
    zookeeper.start()
    kafka.startup()
    log.info("Kafka started")
  }

  def stop() = {
    log.info("Stopping Kafka..")
    kafka.shutdown()
    kafka.awaitShutdown()
    zkUtils.close()
    zookeeper.close()
    zookeeper.stop()
    FileUtils.deleteDirectory(logDir)
    log.info("Kafka stopped")
  }
}

object KafkaZookeeper {

  def apply(port: Int = 9092, zkPort: Int = 2181): KafkaZookeeper = new KafkaZookeeper(port, zkPort)

  /**
    * Run KafkaZookeeper in standalone mode
    */
  def main(args: Array[String]) {
    val config = getOptions("integration.conf")
    val kafkaPort = config.getInt(kafkaBrokerPort)
    val zookeeperPort = config.getInt(kafkaZookeeperPort)
    val allTweetsTopic = config.getString(kafkaTweetsAllTopic)
    val analyzedTweetsTopic = config.getString(kafkaTweetsPredictedSentimentTopic)

    val kafkaZookeeper = KafkaZookeeper(kafkaPort, zookeeperPort)
    kafkaZookeeper.start()
    kafkaZookeeper.createTopic(allTweetsTopic, 3, 1)
    kafkaZookeeper.createTopic(analyzedTweetsTopic, 3, 1)

    val sc = new Scanner(System.in)

    val stopCmd = "bye"
    while (!stopCmd.equals(sc.nextLine())) {
      println(s"use $stopCmd to stop Kafka server")
    }
  }
}