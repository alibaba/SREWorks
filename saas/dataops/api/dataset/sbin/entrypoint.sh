#!/bin/sh

/app/sbin/run.sh

nohup python /app/postrun/00_init_job.py >postrun.log 2>&1 &