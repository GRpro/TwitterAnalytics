package kafka

import java.io.File
import java.util.{Properties, Scanner, UUID}

import scala.util.Random
import org.apache.commons.io.FileUtils
import org.apache.curator.test.TestingServer
import org.apache.log4j.Logger
import kafka.admin.AdminUtils
import kafka.server.{KafkaConfig, KafkaServerStartable}
import kafka.utils.ZkUtils

/**
  * Implementation on single-broker Kafka cluster
  * @param port
  * @param zkPort
  * @param log
  */
case class EmbeddedKafka(port: Int = 9092, zkPort: Int = 2181)(implicit val log: Logger = Logger.getLogger("kafka.EmbeddedKafka")) {
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

object EmbeddedKafka {
  def main(args: Array[String]) {
    val kafka = new EmbeddedKafka()
    kafka.start()

    kafka.createTopic("unclassified-tweets", 3, 1)

    val stopCmd = "bye"
    val sc = new Scanner(System.in)
    while (!"bye".equals(sc.nextLine())) {
      println(s"use $stopCmd to stop Kafka server")
    }

    kafka.stop()
  }
}