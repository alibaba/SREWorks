#!/bin/sh

set -e
set -x

exec java -XX:ActiveProcessorCount=2 -Dloader.path=/app/ -jar /app/action.jar --spring.config.location=/app/
