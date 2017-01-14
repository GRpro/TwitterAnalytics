#!/bin/bash

# Entry point to run application in Docker containers.
# 1. Build project
# 2. Run docker environment

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Build Spark applications jars"

echo "project analytics
clean
assembly
exit" | sbt

echo "Build Play application distribution"

echo "project webapp
clean
playUpdateSecret
dist
exit" | sbt

echo "Run HDFS, Spark, Kafka and webapp"

docker-compose -f ${DIR}/docker-compose.yml up -d --force-recreate \
zookeeper \
kafka \
hdfs-namenode \
hdfs-datanode \
spark-master \
spark-slave \
webapp

echo "Wait until HDFS is started"
sleep 60

echo "Run Spark applications in interactive mode"

docker-compose -f ${DIR}/docker-compose.yml up --force-recreate --no-deps \
spark-twitter-consumer-app \
spark-analytics-app
