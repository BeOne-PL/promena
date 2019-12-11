#!/usr/bin/env bash
set -e

if [[ $@ ]]; then
  exec "$@"
else
  : ${JAVA_OPTS_MEMORY:='-XX:MinRAMPercentage=50 -XX:MaxRAMPercentage=80'}
  : ${JAVA_OPTS_GC:=''}
  : ${JAVA_OPTS_DEBUG_ENABLED:='false'}
  : ${JAVA_OPTS_DEBUG:='-agentlib:jdwp=transport=dt_socket,address=*:9999,suspend=n,server=y'}
  : ${JAVA_OPTS_ADDITIONAL:='-Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom'}
  : ${JAVA_OPTS_CUSTOM:=''}

  if [ $JAVA_OPTS_DEBUG_ENABLED = "true" ]; then DEBUG=$JAVA_OPTS_DEBUG; else DEBUG=""; fi
  CMD="java $JAVA_OPTS_MEMORY $JAVA_OPTS_GC $JAVA_OPTS_ADDITIONAL $JAVA_OPTS_CUSTOM $DEBUG -jar /opt/$APP_JAR"

  echo "# Running <$CMD>..."
  exec bash -c "$CMD"
fi
