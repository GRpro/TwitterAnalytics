#!/bin/bash

# get script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd ${DIR} && docker-compose stop