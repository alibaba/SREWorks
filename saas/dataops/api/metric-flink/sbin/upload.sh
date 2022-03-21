#!/bin/bash

SW_ROOT=$(cd `dirname $0`; pwd)

export MINIO_ENDPOINT=${MINIO_ENDPOINT}
export MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY}
export MINIO_SECRET_KEY=${MINIO_SECRET_KEY}

$SW_ROOT/mc alias set sw http://${MINIO_ENDPOINT} ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY}

###### UPLOAD BUILD-IN RULES CONF
$SW_ROOT/mc mb -p sw/metric-rules
$SW_ROOT/mc cp /app/sbin/vvp-resources/rules.json sw/metric-rules/sreworks/metric/

###### UPLOAD MINIO UDF ARTIFACT JAR
$SW_ROOT/mc mb -p sw/vvp
$SW_ROOT/mc cp /app/sbin/metric-alarm-1.2-SNAPSHOT.jar sw/vvp/artifacts/namespaces/default/udfs/

