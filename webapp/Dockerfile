FROM java:openjdk-8-jre
MAINTAINER Grigory Rozhkov

RUN mkdir /app

COPY target/universal/twitteranalytics-webapp*.zip /

RUN unzip /twitteranalytics-webapp*.zip -d /app/
RUN mv /app/twitteranalytics-webapp* /app/twitteranalytics-webapp

EXPOSE 9000
