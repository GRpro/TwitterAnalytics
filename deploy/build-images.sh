#!/bin/bash

# Script to build all required images for running HDFS and Spark clusters
# Usage:
# ./build-images.sh [scratch]
# Options argument scratch for deleting existing images and building the images from scratch

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ "$1" == scratch ]; then
  docker rmi -f hadoop-spark hdfs-namenode hdfs-datanode spark-master spark-slave
fi

docker build -t hadoop-spark ${DIR}/hadoop-spark/
docker build -t hdfs-namenode ${DIR}/hdfs-namenode/
docker build -t hdfs-datanode ${DIR}/hdfs-datanode/
docker build -t spark-master ${DIR}/spark-master/
docker build -t spark-slave ${DIR}/spark-slave/
docker build -t kafka-zookeeper ${DIR}/kafka-zookeeper/
