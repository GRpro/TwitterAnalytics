import sbt._ // TODO remove wildcard import

object dependencies {

  // versions
  val sparkVersion = "2.0.1"
  val sparkCsvVersion = "1.4.0"
  val configVersion = "1.3.0"
  val jacksonVersion = "2.8.1"
  val coreNlpVersion = "3.6.0"

//  val commonDependencies = Seq("com.typesafe" % "config" % configVersion)

  val consumerDependencies = Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-streaming-twitter" % "1.6.2",
    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
  )


//  val otherDependencies = Seq(
//    "com.databricks" %% "spark-csv" % sparkCsvVersion,
//    "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion,
//    "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion classifier "models",
//    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
//  )
}