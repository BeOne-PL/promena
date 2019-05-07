FROM openjdk:11.0.3-jre-stretch

RUN apt-get -y update && \
    apt-get -y upgrade && \
    apt-get -y install maven && \
    apt-get -y clean

${DOCKERFILE-FRAGMENT}