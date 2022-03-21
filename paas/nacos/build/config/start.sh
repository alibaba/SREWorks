#!/bin/bash

set -x

sleep 20s

sh ./render.sh run.py.tpl run.py

if [ $? != 0 ]; then
  exit 1
fi

python ./run.py

