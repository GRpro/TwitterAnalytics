#!/bin/bash

if [ ! -d "$KAFKA_HOME" ]; then
    # Control will enter here if $DIRECTORY doesn't exist.
    sh /usr/bin/install_kafka.sh
fi

# Set the external host and port
if [ ! -z "$ADVERTISED_HOST" ]; then
  echo "advertised host: $ADVERTISED_HOST"
  echo "advertised.host.name=$ADVERTISED_HOST" >> $KAFKA_HOME/config/server.properties
  sed -r -i "s/#(advertised.host.name)=(.*)/\1=$ADVERTISED_HOST/g" $KAFKA_HOME/config/server.properties
fi
if [ ! -z "$ADVERTISED_PORT" ]; then
  echo "advertised port: $ADVERTISED_PORT"
  echo "advertised.port=$ADVERTISED_PORT" >> $KAFKA_HOME/config/server.properties
  sed -r -i "s/#(advertised.port)=(.*)/\1=$ADVERTISED_PORT/g" $KAFKA_HOME/config/server.properties
fi

cd $KAFKA_HOME

nohup $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties  > /tmp/zookeeper.log 2>&1 &


echo "Waiting for zookeeper to launch on 2181..."

while ! nc -z localhost 2181; do
    sleep 0.1 # wait for 1/10 of the second before check again
  done

echo "zookeeper launched"

nohup $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties > /tmp/kafka.log 2>&1 &

echo "Waiting kafka to launch on 9092..."

while ! nc -z localhost 9092; do
    sleep 0.1 # wait for 1/10 of the second before check again
  done

echo "kafka launched"

./bin/kafka-topics.sh  --create --zookeeper localhost:2181 --replication-factor 1 --partition 1 --topic log

tail -f /tmp/kafka.log