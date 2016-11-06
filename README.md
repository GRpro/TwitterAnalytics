Twitter analytics project
=========================

Description
-----------
Soon

Setup environment
-----------------

1. Install docker `https://docs.docker.com/engine/installation/`
2. Install docker-compose `https://docs.docker.com/compose/install/`
3. Execute `./spark_cluster/start.sh`. The script builds base docker image
   and starts 2 containers: spark-master and spark-worker.

Build project and run
---------------------

1. Enter sbt shell at the project root directory. Run `clean` and then `assembly` to build jars for Spark job
2. Submit consumer job `./consumer/submit.sh`
3. navigate to `localhost:8080` to see job running
