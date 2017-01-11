#!/bin/bash

# Load training datasets

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DATASETS_DIR=${DIR}/datasets

if [ ! -d ${DATASETS_DIR} ]; then
    URL='http://cs.stanford.edu/people/alecmgo/trainingandtestdata.zip'
    wget -O /tmp/trainingandtestdata.zip ${URL}
    unzip /tmp/trainingandtestdata.zip -d ${DIR}/datasets

else
    echo "Directory ${DATASETS_DIR} already exists"
fi

HDFS_URI="hdfs://hdfs-namenode:9000/datasets"

hadoop fs -test -d $HDFS_URI

if [ $? != 0 ]; then

echo "Copy datasets to $HDFS_URI "
hadoop fs -mkdir -p $HDFS_URI
hadoop fs -chmod -R 777 $HDFS_URI
hadoop fs -put ${DATASETS_DIR}/* $HDFS_URI

fi
