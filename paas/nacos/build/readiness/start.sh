#!/bin/sh

set -x

http_code=$(curl -I -m 10 -o /dev/null -s -w %{http_code} http://${ENDPOINT_PAAS_NACOS}/nacos/readiness)
echo $http_code
if [ "$http_code" != "200" ]; then
    echo "failed"
    exit 1
fi
echo "success"