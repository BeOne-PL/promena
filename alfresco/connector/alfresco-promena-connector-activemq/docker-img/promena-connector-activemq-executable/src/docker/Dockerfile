FROM azul/zulu-openjdk-centos:11.0.4

RUN yum clean all && rm -rf /tmp/* /var/tmp/* /var/cache/yum/*

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