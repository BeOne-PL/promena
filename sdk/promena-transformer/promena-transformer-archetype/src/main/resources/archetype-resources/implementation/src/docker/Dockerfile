FROM azul/zulu-openjdk-centos:11.0.5

RUN echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen && \
    localedef --quiet -c -i en_US -f UTF-8 en_US.UTF-8

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US
ENV LC_ALL en_US.UTF-8

ENV MAVEN_VERSION 3.6.3

RUN yum install -y wget && \
    # Maven
    cd opt && \
    wget https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    tar xzf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    rm -f apache-maven-$MAVEN_VERSION-bin.tar.gz

ENV JRE_HOME /usr/lib/jvm/zulu-11
ENV JAVA_HOME /usr/lib/jvm/zulu-11
ENV M2_HOME /opt/apache-maven-$MAVEN_VERSION
ENV PATH ${M2_HOME}/bin:${PATH}

${DOCKERFILE-FRAGMENT}

RUN yum clean all && rm -rf /tmp/* /var/tmp/* /var/cache/yum/*