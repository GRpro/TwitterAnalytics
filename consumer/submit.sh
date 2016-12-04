#!/bin/bash

# Submits consumer job to spark cluster running in docker containers.
# Set variable CONSUMER_JAR to the location of jar file with spark consumer application.
# If the variable is not defined the jar is looked for within target directory.

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z $CONSUMER_JAR ]; then
    CONSUMER_JAR=$( ls $DIR/target/scala-2.11/TwitterAnalytics-consumer-assembly* )
fi

if [ ! -f $CONSUMER_JAR ]; then
    echo "No such jur $CONSUMER_JAR"
    exit 1
else
    echo "Found jar $CONSUMER_JAR"
fi

SPARK_MASTER_CONTAINER=$( docker ps | grep 'spark-master' | awk '{ print $1 }' )

if [ -z $SPARK_MASTER_CONTAINER ]; then
    echo "Spark master docker container not found. Please run spark cluster and retry. "
    exit 1
else
    echo "Found spark master container $SPARK_MASTER_CONTAINER"
fi

JAR_PATH=/root/${CONSUMER_JAR##*/}

docker cp $CONSUMER_JAR $SPARK_MASTER_CONTAINER:$JAR_PATH

# submit application
docker exec $SPARK_MASTER_CONTAINER bash -x -c "\$SPARK_HOME/bin/spark-submit \
  --class kpi.twitter.analysis.consumer.TwitterConsumer \
  --master spark://spark-master-host:7077 \
  $JAR_PATH"