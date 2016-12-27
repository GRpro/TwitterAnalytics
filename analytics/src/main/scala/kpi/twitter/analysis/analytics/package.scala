package kpi.twitter.analysis

import com.typesafe.config.Config
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer

import kpi.twitter.analysis.utils._

package object analytics {

  def createKafkaProducer(config: Config): KafkaProducer[String, String] = {
    val kafkaProducerConfig = {
      val p = new java.util.Properties()
      p.setProperty("bootstrap.servers", config.getString(kafkaBootstrapServers))
      p.setProperty("key.serializer", classOf[StringSerializer].getName)
      p.setProperty("value.serializer", classOf[StringSerializer].getName)
      p
    }
    new KafkaProducer[String, String](kafkaProducerConfig)
  }
}
