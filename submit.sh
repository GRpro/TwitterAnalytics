#!/bin/bash

# Submits job to running spark cluster in docker containers.
# Set variable ANALYTICS_JAR to the location of jar file with spark consumer application.
# If the variable is not defined the jar is looked for within target directory.


case "$1" in
    -c|--twitter-consumer)
        RUN_CLASS="kpi.twitter.analysis.analytics.TwitterConsumerJob"
    ;;
    -ml|--ml-analyzer)
        RUN_CLASS="kpi.twitter.analysis.analytics.AnalyzerJob"
    ;;
    *)
        # unknown option
        echo "Unknown job. Please use -c|--twitter-consumer or -ml|--ml-analyzer"
        exit 1
    ;;
esac

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z $ANALYTICS_JAR ]; then
    ANALYTICS_JAR=$( ls $DIR/analytics/target/scala-2.11/TwitterAnalytics-analytics-assembly* )
fi

if [ ! -f $ANALYTICS_JAR ]; then
    echo "No such jur $ANALYTICS_JAR"
    exit 1
else
    echo "Found jar $ANALYTICS_JAR"
fi

SPARK_MASTER_CONTAINER=$( docker ps | grep 'spark-master' | awk '{ print $1 }' )

if [ -z $SPARK_MASTER_CONTAINER ]; then
    echo "Spark master docker container not found. Please run spark cluster and retry. "
    exit 1
else
    echo "Found spark master container $SPARK_MASTER_CONTAINER"
fi

JAR_PATH=/root/${ANALYTICS_JAR##*/}

docker cp $ANALYTICS_JAR $SPARK_MASTER_CONTAINER:$JAR_PATH

# submit application
docker exec $SPARK_MASTER_CONTAINER bash -x -c "\$SPARK_HOME/bin/spark-submit \
  --class $RUN_CLASS \
  --master spark://spark-master:7077 \
  --executor-memory 512M \
  --total-executor-cores 2 \
  $JAR_PATH"