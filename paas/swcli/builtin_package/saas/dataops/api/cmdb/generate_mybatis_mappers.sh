#!/usr/bin/env bash

set -x

worker_path=$(cd "$(dirname "$0")"; pwd)

rm -rf ${worker_path}/cmdb-service/src/main/resources/mybatis/mapper/tddl/*.xml

cd ${worker_path}/cmdb-service
mvn mybatis-generator:generate
