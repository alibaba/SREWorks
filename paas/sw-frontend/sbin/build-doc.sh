#!/bin/bash

set -x
set -e

cd /app/docs/
yarn install --registry=${NPM_REGISTRY_URL}
yarn build:doc
