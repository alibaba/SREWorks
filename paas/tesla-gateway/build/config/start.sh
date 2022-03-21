#!/bin/bash

set -x

sh ./render.sh default_route.json.tpl default_route.json

if [ $? != 0 ]; then
  exit 1
fi

#sleep 1000000
python ./run.py

