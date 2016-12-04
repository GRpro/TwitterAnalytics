#!/bin/bash
wget http://www-us.apache.org/dist/kafka/0.10.0.0/kafka_2.11-0.10.0.0.tgz -O /tmp/kafka.tgz
mkdir -p $KAFKA_HOME && cd $KAFKA_HOME 
tar -xvzf /tmp/kafka.tgz --strip 1