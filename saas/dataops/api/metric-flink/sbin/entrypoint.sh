#!/bin/bash

set -x
set -e

/bin/sh /app/sbin/build.sh

/bin/sh /app/sbin/upload.sh

/bin/sh /app/sbin/flink_job_init.sh

python3 /app/sbin/init-kafka.py
