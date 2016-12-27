#!/bin/bash

URL='http://cs.stanford.edu/people/alecmgo/trainingandtestdata.zip'

mkdir -p /opt/datasets

wget -O /tmp/trainingandtestdata.zip ${URL}
unzip /tmp/trainingandtestdata.zip -d /opt/datasets