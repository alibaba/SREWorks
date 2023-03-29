#!/bin/bash

set -e
set -x

export APP_NAME=tesla-appmanager

if [ -z "${JVM_XMX}" ]; then
  export JVM_XMX="4500m"
fi
echo "JVM_XMX: ${JVM_XMX}"


# 启动进程
echo "spring.profiles.active=docker,redis-${APPMANAGER_REDIS_MODE:-standalone}" > /app/config/application.properties
exec java -Xmx${JVM_XMX} -Xms${JVM_XMX} ${APPMANAGER_ADDITIONAL_ARGS} -jar /app/tesla-appmanager-standalone.jar --spring.config.location=file:///app/config/application.properties
