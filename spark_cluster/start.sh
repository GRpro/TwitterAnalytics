#!/bin/bash

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

IMG_NAME="spark-2.0"
echo "Building image $IMG_NAME"
docker build -t ${IMG_NAME} ${DIR}
echo "Starting containers from $IMG_NAME"
cd ${DIR} && docker-compose up -d
