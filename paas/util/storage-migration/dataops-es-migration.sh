#!/bin/sh

set -x
set -e

force="false"
while getopts 'f' OPT; do
    case $OPT in
        f) force="true";;
        ?) func;;
    esac
done

indices=$(curl -XGET -u ${DATA_ES_USER}:${DATA_ES_PASSWORD} http://${DATA_ES_HOST}:${DATA_ES_PORT}/_cat/indices?format=JSON)
echo $indices | jq -c .[].index | while read str_index; do
  index=${str_index: 1: ${#str_index} - 2}
  if [[ $index == .* ]]; then
    echo "==========build-in index:"$index", skip...=========="
  else
    if [[ $force == "true" ]]; then
      index_exist_code=$(curl -IL -u ${DATA_DEST_ES_USER}:${DATA_DEST_ES_PASSWORD} http://${DATA_DEST_ES_HOST}:${DATA_DEST_ES_PORT}/$index?pretty| head -n 1 | cut -d$' ' -f2)
      if [[ $index_exist_code == 200 ]]; then
        echo "==========flush index:"$index", starting...=========="
        curl -XDELETE -u ${DATA_DEST_ES_USER}:${DATA_DEST_ES_PASSWORD} http://${DATA_DEST_ES_HOST}:${DATA_DEST_ES_PORT}/$index
        echo "==========flush index:"$index", end=========="
      fi
    fi
    echo "==========migrate index:"$index", starting...=========="
    elasticdump --input=http://${DATA_ES_USER}:${DATA_ES_PASSWORD}@${DATA_ES_HOST}:${DATA_ES_PORT}/$index --output=http://${DATA_DEST_ES_USER}:${DATA_DEST_ES_PASSWORD}@${DATA_DEST_ES_HOST}:${DATA_DEST_ES_PORT}/$index --type=settings

    #elasticdump --input=http://${DATA_ES_USER}:${DATA_ES_PASSWORD}@${DATA_ES_HOST}:${DATA_ES_PORT}/$index --output=http://${DATA_DEST_ES_USER}:${DATA_DEST_ES_PASSWORD}@${DATA_DEST_ES_HOST}:${DATA_DEST_ES_PORT}/$index --type=mapping
    elasticdump --input=http://${DATA_ES_USER}:${DATA_ES_PASSWORD}@${DATA_ES_HOST}:${DATA_ES_PORT}/$index --output=${index}.json --type=mapping
    index_mapping=$(jq '.[].mappings' ${index}.json)
    echo $index_mapping | curl -XPUT -H 'Content-Type: application/json' -u ${DATA_DEST_ES_USER}:${DATA_DEST_ES_PASSWORD} http://${DATA_DEST_ES_HOST}:${DATA_DEST_ES_PORT}/$index/_mapping -d @-
    rm -f ${index}.json
    elasticdump --input=http://${DATA_ES_USER}:${DATA_ES_PASSWORD}@${DATA_ES_HOST}:${DATA_ES_PORT}/$index --output=http://${DATA_DEST_ES_USER}:${DATA_DEST_ES_PASSWORD}@${DATA_DEST_ES_HOST}:${DATA_DEST_ES_PORT}/$index --type=data
    echo "==========migrate index:"$index", end=========="
  fi
done
