FROM azul/zulu-openjdk-centos:11.0.4

ENV MAVEN_VERSION 3.6.1

RUN yum update -y && \
    yum install -y wget && \
    # Maven
    cd opt && \
    wget https://www-eu.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    tar xzf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    rm -f apache-maven-$MAVEN_VERSION-bin.tar.gz

ENV JRE_HOME /usr/lib/jvm/zulu-11
ENV JAVA_HOME /usr/lib/jvm/zulu-11
ENV M2_HOME /opt/apache-maven-3.6.1
ENV PATH ${M2_HOME}/bin:${PATH}

${DOCKERFILE-FRAGMENT}

RUN yum clean all && rm -rf /tmp/* /var/tmp/* /var/cache/yum/*