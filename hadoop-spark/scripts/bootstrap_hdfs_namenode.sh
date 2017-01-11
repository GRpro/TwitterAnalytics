#!/bin/bash

# Start the SSH daemon
service ssh restart

# Setup password less ssh
sshpass -p screencast ssh-copy-id root@localhost

export HOSTNAME=`hostname`
sed -i "s#localhost#$HOSTNAME#g" /opt/hadoop-${HADOOP_VERSION}/etc/hadoop/core-site.xml

# Format the NameNode data directory
hdfs namenode -format -force

# Start HDFS services
start-dfs.sh

# Run in daemon mode, don't exit
while true; do
  sleep 100;
done
