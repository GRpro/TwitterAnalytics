#!/bin/bash
# Show docker environment IP addresses

KAFKA_CONTAINERS=$(docker ps | grep 'kafka' | awk '{ print $1 }')
ZOOKEEPER_CONTAINER=$(docker ps | grep 'zookeeper' | awk '{ print $1 }')
SPARK_MASTER_CONTAINER=$(docker ps | grep 'spark-master' | awk '{ print $1 }')
HDFS_NAMENODE_CONTAINER=$(docker ps | grep 'namenode' | awk '{ print $1 }')
WEB_UI_CONTAINER=$(docker ps | grep 'webapp' | awk '{ print $1 }')

echo "@@@ Kafka broker IPs"
for cid in $KAFKA_CONTAINERS ; do
    docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $cid
done

echo "@@@ Kafka zookeeper IP"
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $ZOOKEEPER_CONTAINER

echo "@@@ Spark master IP"
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $SPARK_MASTER_CONTAINER

echo "@@@ HDFS namenode IP"
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $HDFS_NAMENODE_CONTAINER

#echo "Twitter analytics Web UI IP"
#docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $WEB_UI_CONTAINER