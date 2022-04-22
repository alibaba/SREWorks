#!/bin/bash

SW_ROOT=$(cd `dirname $0`; pwd)

export DATA_DB_HOST=${DATA_DB_HOST}
export DATA_DB_PORT=${DATA_DB_PORT}
export DATA_DB_HEALTH_NAME=${DATA_DB_HEALTH_NAME}
export DATA_DB_USER=${DATA_DB_USER}
export DATA_DB_PASSWORD=${DATA_DB_PASSWORD}

export HEALTH_ENDPOINT=${HEALTH_ENDPOINT}

export MINIO_ENDPOINT=${MINIO_ENDPOINT}
export MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY}
export MINIO_SECRET_KEY=${MINIO_SECRET_KEY}

envsubst < /app/sbin/common.properties.tpl > /app/sbin/common.properties

cd /app/sbin && jar uvf metric-alarm-1.2-SNAPSHOT.jar common.properties
