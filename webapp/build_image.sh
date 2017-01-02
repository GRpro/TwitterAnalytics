#!/bin/bash

echo "Build Web application image"

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd ${DIR}/.. && echo "project webapp
clean
playUpdateSecret
dist
exit" | sbt

docker build -t twitter-analytics-webapp ${DIR}