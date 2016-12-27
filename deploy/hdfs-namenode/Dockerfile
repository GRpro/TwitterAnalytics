FROM hadoop-spark
MAINTAINER Grigory Rozhkov

COPY scripts /usr/bin/.

RUN /usr/bin/load_datasets.sh

ENTRYPOINT ["/usr/bin/bootstrap.sh"]
