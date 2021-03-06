import sbt._ // TODO remove wildcard import

object dependencies {

  // versions
  val sparkVersion = "2.0.1"
  val sparkCsvVersion = "1.4.0"
  val configVersion = "1.3.0"
  val jacksonVersion = "2.8.1"
  val coreNlpVersion = "3.6.0"
  val scalaTestVersion = "3.0.0"
  val kafkaVersion = "0.10.1.0"

  val utilsDependencies = Seq(
//    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
    "com.typesafe" % "config" % configVersion,
    "com.google.code.gson" % "gson" % "2.8.0",
    "org.twitter4j" % "twitter4j-stream" % "4.0.4"
  )

  val analyticsDependencies = Seq(
    // provided
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
    ("org.apache.bahir" %% "spark-streaming-twitter" % sparkVersion)
      exclude ("org.spark-project.spark", "unused"),
    ("org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion)
      exclude ("org.spark-project.spark", "unused")
      exclude ("org.scalatest", "scalatest"),
    // test
    "org.apache.spark" %% "spark-core" % sparkVersion % "test",
    "org.apache.bahir" %% "spark-streaming-twitter" % sparkVersion % "test",
    "org.apache.spark" %% "spark-sql" % sparkVersion % "test",
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  )

  val toolsDependencies = Seq(
    /* Kafka dependencies */
    "org.apache.kafka" %% "kafka" % kafkaVersion,
//    "org.apache.commons" % "commons-io" % "1.3.2",
    "org.apache.curator" % "curator-test" % "3.2.0",
    /* Spark dependencies */
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    "org.apache.spark" %% "spark-core" % sparkVersion,
//    "org.apache.bahir" %% "spark-streaming-twitter" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-mllib" % sparkVersion
  )

  val webappDependencies = Seq(
    "org.apache.kafka" % "kafka-clients" % kafkaVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
  )
}
