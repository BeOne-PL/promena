#!/bin/sh

export COMPOSE_FILE_PATH=${PWD}/target/classes/docker/docker-compose.yml

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    docker volume create alfresco-promena-predefined-rendition-acs-volume
    docker volume create alfresco-promena-predefined-rendition-db-volume
    docker volume create alfresco-promena-predefined-rendition-ass-volume
    docker volume create alfresco-promena-predefined-rendition-activemq-volume
    docker-compose -f $COMPOSE_FILE_PATH up --build -d
}

down() {
    if [ -f $COMPOSE_FILE_PATH ]; then
        docker-compose -f $COMPOSE_FILE_PATH down -v
    fi
}

purge() {
    docker-compose -f $COMPOSE_FILE_PATH rm -v -s -f
    docker volume rm -f alfresco-promena-predefined-rendition-acs-volume
    docker volume rm -f alfresco-promena-predefined-rendition-db-volume
    docker volume rm -f alfresco-promena-predefined-rendition-ass-volume
    docker volume rm -f alfresco-promena-predefined-rendition-activemq-volume
}

build() {
    $MVN_EXEC -DskipTests=true clean package
    $MVN_EXEC -DskipTests=true -f docker-img/promena-predefined-rendition-executable/pom.xml clean package
}

tail() {
    docker-compose -f $COMPOSE_FILE_PATH logs -f
}

tail_all() {
    docker-compose -f $COMPOSE_FILE_PATH logs --tail="all"
}

prepare_test() {
    $MVN_EXEC -DskipTests=true verify
}

test() {
    $MVN_EXEC -DskipTests=true verify
}

case "$1" in
  build_start)
    down
    build
    start
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
    echo "Usage: $0 {build_start|build_start_it_supported|start|stop|purge|tail|build_test|test}"
esac