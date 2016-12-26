#!/bin/bash

# Submits consumer job to spark cluster running in docker containers.

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z $ANALYZER_JAR ]; then
    ANALYZER_JAR=$( ls $DIR/target/scala-2.11/TwitterAnalytics-analyzer-assembly* )
fi

if [ ! -f $ANALYZER_JAR ]; then
    echo "No such jar $ANALYZER_JAR"
    exit 1
else
    echo "Found jar $ANALYZER_JAR"
fi

SPARK_MASTER_CONTAINER=$( docker ps | grep 'spark-master' | awk '{ print $1 }' )

if [ -z $SPARK_MASTER_CONTAINER ]; then
    echo "Spark master docker container not found. Please run spark cluster and retry. "
    exit 1
else
    echo "Found spark master container $SPARK_MASTER_CONTAINER"
fi

JAR_PATH=/root/${ANALYZER_JAR##*/}

docker cp $ANALYZER_JAR $SPARK_MASTER_CONTAINER:$JAR_PATH

# submit application
docker exec $SPARK_MASTER_CONTAINER bash -x -c "\$SPARK_HOME/bin/spark-submit \
  --class kpi.twitter.analysis.ml.NaiveBayesModelCreator \
  --master spark://spark-master:7077 \
  $JAR_PATH"