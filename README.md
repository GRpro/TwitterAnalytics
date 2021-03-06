Twitter analytics project
=========================

Description
-----------
Twitter analytics application on top of Spark and Kafka.
Currently there is an implementation of consuming part.

Twitter Stream ------> Spark Streaming Job ------> Apache Kafka

Please follow next steps to have it up and running.

Setup environment
-----------------

1. Install [docker](https://docs.docker.com/engine/installation/)
2. Install latest version of [docker-compose](https://github.com/docker/compose/releases)
3. Build docker images `./deploy/build-images.sh`. 
   This requires about 20 minutes on my environment.
4. Run HDFS, Spark and Kafka within docker containers that were built 
   in step 3 by executing `cd deploy && docker-compose up -d`
   
For more details how to manage running docker containers 
refer to [project deployment](deploy/README.md) section

Build project and run
---------------------

1. Setup correct twitter OAuth credentials in `./consumer/src/main/resources/application.conf`. 
   If you do not have Twitter App credentials please register [new ones](https://dev.twitter.com/oauth/overview/application-owner-access-tokens) and set them properly
      - twitter.consumerKey="xxx"
      - twitter.consumerSecret="xxx"
      - twitter.accessToken="xxx"
      - twitter.accessTokenSecret="xxx"
2. Enter sbt shell at the project root directory. Run `clean` and then `assembly` to build jars for Spark job
3. Submit consumer job `./consumer/submit.sh`
4. Navigate to Spark UI on `localhost:8080` to see job running
5. Attach to kafka-zookeeper container `docker exec -it deploy_kafka-zookeeper_1 /bin/bash` 
   (See [project deployment](deploy/README.md) for more details) and start console 
   consumer to see incoming tweets `${KAFKA_HOME}/kafka-console-consumer.sh --topic unclassified-tweets --new-consumer --bootstrap-server localhost:9092`

Compose deployment
------------------

# Docker compose for spawning on demand HDFS and Spark clusters

# Build the required Docker images
`./build-images.sh`

# Run the cluster
Note: Run below commands from the directory where `docker-compose.yml` file is present.
## bring up the cluster
`docker-compose up -d`
## stop the cluster
`docker-compose stop`
## restart the stopped cluster
`docker-compose start`
## remove containers
`docker-compose rm -f`
## to scale HDFS datanode or Spark worker containers
`docker-compose scale spark-slave=n` where n is the new number of containers.

## Attaching to cluster containers
  - HDFS NameNode container
    * Runs HDFS NameNode and DataNode services
    * `docker exec -it deploy_hdfs-namenode_1 /bin/bash`
  - HDFS DataNode container(s)
    * Runs HDFS DataNode service
    * There could be multiple instances of this container. To connect to n'th container
      * `docker exec -it deploy_hdfs-datanode__n_ /bin/bash`
  - Spark Master container
    * Runs Spark Master and Worker services
    * `docker exec -it deploy_spark-master_1 /bin/bash`
  - Spark Worker container
    * Runs Spark Worker service
    * There could be multiple instances of this container. To connect to n'th container
      * `docker exec -it deploy_spark-slave__n_ /bin/bash`
  - Kafka Zookeeper container
    * Runs Zookeeper and Kafka Broker
      * `docker exec -it deploy_kafka-zookeeper_1 /bin/bash`
