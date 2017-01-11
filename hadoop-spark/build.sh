#!/bin/bash

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker build -t hadoop-spark:${TAG:-0.1} ${DIR}