Twitter analytics project
=========================

Description
-----------
This should be a great example of twitter analytics application on top of Spark and Kafka.
Currently there is an implementation of consuming part.

Twitter Stream ------> Spark Streaming Job ------> Apache Kafka

Please follow next steps to have it up and running.

Setup environment
-----------------

1. Install docker `https://docs.docker.com/engine/installation/`
2. Install docker-compose `https://docs.docker.com/compose/install/`
3. Execute `./spark_cluster/start.sh`. The script builds base docker image
   and starts 2 containers: spark-master and spark-worker.
4. Download and unpack Apache Kafka distribution following this link http://apache.volia.net/kafka/0.10.1.0/kafka_2.11-0.10.1.0.tgz.
   Start zookeeper `cd ./kafka_2.11-0.10.1.0/bin` `./zookeeper-server-start.sh ../config/zookeeper.properties`. 
   From another terminal start kafka broker `cd ./kafka_2.11-0.10.1.0/bin` `./kafka-server-start.sh ../config/server.properties`

Build project and run
---------------------

1. Setup correct twitter OAuth credentials in `./consumer/src/main/resources/application.conf`. 
   If you do not have Twitter App credentials please register new ones https://dev.twitter.com/oauth/overview/application-owner-access-tokens
   and set them properly ```
   twitter.consumerKey="xxx"
   twitter.consumerSecret="xxx"
   twitter.accessToken="xxx"
   twitter.accessTokenSecret="xxx"
    ```
2. Enter sbt shell at the project root directory. Run `clean` and then `assembly` to build jars for Spark job
3. Create Kafka topic `./kafka-topics.sh --create --topic tweets-1 --partitions 3 --replication-factor 1 --zookeeper localhost:2181`
4. Start from another terminal console consumer `./kafka-console-consumer.sh --topic tweets-1 --new-consumer --bootstrap-server localhost:9092`
3. Submit consumer job `./consumer/submit.sh`
4. navigate to Spark UI on `localhost:8080` to see job running
5. See incoming tweets from console consumer
