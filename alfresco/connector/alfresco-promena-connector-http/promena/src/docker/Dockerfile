FROM azul/zulu-openjdk-centos:11.0.5

RUN echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen && \
    localedef --quiet -c -i en_US -f UTF-8 en_US.UTF-8

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US
ENV LC_ALL en_US.UTF-8

ENV JRE_HOME /usr/lib/jvm/zulu-11
ENV JAVA_HOME /usr/lib/jvm/zulu-11

${DOCKERFILE-FRAGMENT}

RUN yum clean all && rm -rf /tmp/* /var/tmp/* /var/cache/yum/*

EXPOSE 8080

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT [ "/docker-entrypoint.sh" ]

ENV APP_JAR ${APP_JAR}
ADD $APP_JAR /opt