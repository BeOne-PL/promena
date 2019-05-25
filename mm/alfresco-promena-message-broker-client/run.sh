#!/bin/sh

export COMPOSE_FILE_PATH=${PWD}/target/classes/docker/docker-compose.yml

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    docker volume create alfresco-promena-message-broker-client-acs-volume
    docker volume create alfresco-promena-message-broker-client-db-volume
    docker volume create alfresco-promena-message-broker-client-ass-volume
    docker volume create alfresco-promena-message-broker-client-activemq-log-volume
    docker volume create alfresco-promena-message-broker-client-activemq-conf-volume
    docker volume create alfresco-promena-message-broker-client-activemq-data-volume
    docker-compose -f $COMPOSE_FILE_PATH up --build -d
}

down() {
    if [ -f $COMPOSE_FILE_PATH ]; then
        docker-compose -f $COMPOSE_FILE_PATH down
    fi
}

stop_acs() {
    docker-compose -f $COMPOSE_FILE_PATH stop alfresco-promena-message-broker-client-acs
    docker-compose -f $COMPOSE_FILE_PATH rm -f alfresco-promena-message-broker-client-acs
}

start_acs() {
    docker-compose -f $COMPOSE_FILE_PATH up --build -d
    docker-compose -f $COMPOSE_FILE_PATH create alfresco-promena-message-broker-client-acs
    docker-compose -f $COMPOSE_FILE_PATH start alfresco-promena-message-broker-client-acs
}

purge() {
    docker volume rm -f alfresco-promena-message-broker-client-acs-volume
    docker volume rm -f alfresco-promena-message-broker-client-db-volume
    docker volume rm -f alfresco-promena-message-broker-client-ass-volume
    docker volume rm -f alfresco-promena-message-broker-client-activemq-log-volume
    docker volume rm -f alfresco-promena-message-broker-client-activemq-conf-volume
    docker volume rm -f alfresco-promena-message-broker-client-activemq-data-volume
}

build() {
    $MVN_EXEC -DskipTests=true clean package
}

tail() {
    docker-compose -f $COMPOSE_FILE_PATH logs -f
}

tail_all() {
    docker-compose -f $COMPOSE_FILE_PATH logs --tail="all"
}

prepare_test() {
    $MVN_EXEC verify -DskipTests=true
}

test() {
    $MVN_EXEC verify
}

case "$1" in
  build_start)
    down
    build
    start
    tail
    ;;
  build_acs)
    stop_acs
    build
    start_acs
    tail
    ;;
  build_start_it_supported)
    down
    build
    prepare_test
    start
    tail
    ;;
  start)
    start
    tail
    ;;
  stop)
    down
    ;;
  purge)
    down
    purge
    ;;
  tail)
    tail
    ;;
  build_test)
    down
    build
    prepare_test
    start
    test
    tail_all
    down
    ;;
  test)
    test
    ;;
  *)
    echo "Usage: $0 {build_start|build_acs|build_start_it_supported|start|stop|purge|tail|build_test|test}"
esac