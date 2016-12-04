# Kafka and Zookeeper
FROM java:openjdk-8-jre
MAINTAINER Grigory Rozhkov

ENV DEBIAN_FRONTEND noninteractive
ENV KAFKA_HOME /opt/kafka

RUN apt-get update -y  && apt-get install netcat -y

ADD scripts/install_kafka.sh /usr/bin/install_kafka.sh
ADD scripts/start_kafka.sh /usr/bin/start_kafka.sh

RUN sh /usr/bin/install_kafka.sh

# 2181 is zookeeper, 9092 is kafka
EXPOSE 2181 9092

CMD ["sh", "/usr/bin/start_kafka.sh"]