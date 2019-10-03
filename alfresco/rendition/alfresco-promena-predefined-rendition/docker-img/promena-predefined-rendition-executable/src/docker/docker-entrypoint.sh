#!/usr/bin/env bash
set -e

: ${JAVA_OPTS_MEMORY:='-Xms1536m -Xmx1536m -XX:NewSize=512m -XX:MaxNewSize=512m'}
: ${JAVA_OPTS_GC:=''}
: ${JAVA_OPTS_DEBUG:='-agentlib:jdwp=transport=dt_socket,address=*:9999,suspend=n,server=y'}
: ${JAVA_OPTS_ADDITIONAL:='-Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom'}

if [[ $@ ]]; then
    exec "$@"
else
    CMD="java $JAVA_OPTS_MEMORY $JAVA_OPTS_GC $JAVA_OPTS_ADDITIONAL $JAVA_OPTS_DEBUG -jar /opt/$APP_JAR"

    echo "# Running <$CMD>..."
    exec bash -c "$CMD"
fi