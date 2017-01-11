#!/bin/bash

# Entry point to run application in Docker containers.
# 1. Download datasets
# 2. Build project
# 3. Run docker environment

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Build Spark applications jars"

echo "project analytics
clean
assembly
exit" | sbt

echo "Build Play application distribution"

echo "project webapp
clean
playUpdateSecret
dist
exit" | sbt

echo "Start Docker cluster"

docker-compose up -d --force-recreate