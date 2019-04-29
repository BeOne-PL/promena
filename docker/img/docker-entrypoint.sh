#!/usr/bin/env bash
set -e

: ${ENV_FILE:='/tmp/environment'}
: ${JAVA_OPTS_MEMORY:='-Xms1536m -Xmx1536m -XX:NewSize=512m -XX:MaxNewSize=512m'}
: ${JAVA_OPTS_GC:=''}
: ${JAVA_OPTS_DEBUG:='-agentlib:jdwp=transport=dt_socket,address=*:9999,suspend=n,server=y'}
: ${JAVA_OPTS_ADDITIONAL:='-Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom'}

function getApplicationProperties() {
    parameters=""

    if env | grep app.
    then
        env | grep app. > ${ENV_FILE}

        while IFS='=' read -r key value
        do
            property=${key:4}

            parameters="$parameters --$property=$value"
        done < ${ENV_FILE}

        rm ${ENV_FILE}
    fi
}

getApplicationProperties

if [[ $@ ]]; then
    exec "$@"
else
    cmd="java $JAVA_OPTS_MEMORY $JAVA_OPTS_GC $JAVA_OPTS_ADDITIONAL $JAVA_OPTS_DEBUG -jar promena-executable-$APP_VERSION.jar $parameters"

    echo "# Running <$cmd>..."
    cd /opt && $cmd
fi