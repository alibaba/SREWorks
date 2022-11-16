apiVersion: core.oam.dev/v1alpha2
kind: ApplicationConfiguration
metadata:
  annotations:
    appId: search
    clusterId: master
    namespaceId: ${NAMESPACE_ID}
    stageId: prod
  name: search
spec:
  components:
  - dataInputs: []
    dataOutputs: []
    dependencies: []
    parameterValues:
    - name: STAGE_ID
      toFieldPaths:
      - spec.stageId
      value: prod
    revisionName: INTERNAL_ADDON|productopsv2|_
    scopes:
    - scopeRef:
        apiVersion: apps.abm.io/v1
        kind: Cluster
        name: '{{ Global.CLUSTER_ID }}'
    - scopeRef:
        apiVersion: apps.abm.io/v1
        kind: Namespace
        name: '{{ Global.NAMESPACE_ID }}'
    - scopeRef:
        apiVersion: apps.abm.io/v1
        kind: Stage
        name: '{{ Global.STAGE_ID }}'
    traits: []
  - parameterValues:
    - name: STAGE_ID
      toFieldPaths:
      - spec.stageId
      value: prod
    revisionName: INTERNAL_ADDON|developmentmeta|_
    scopes:
    - scopeRef:
        apiVersion: apps.abm.io/v1
        kind: Cluster
        name: '{{ Global.CLUSTER_ID }}'
    - scopeRef:
        apiVersion: apps.abm.io/v1
        kind: Namespace
        name: '{{ Global.NAMESPACE_ID }}'
    - scopeRef:
        apiVersion: apps.abm.io/v1
        kind: Stage
        name: '{{ Global.STAGE_ID }}'
  - parameterValues:
    - name: STAGE_ID
      toFieldPaths:
      - spec.stageId
      value: prod
    - name: OVERWRITE_IS_DEVELOPMENT
      toFieldPaths:
      - spec.overwriteIsDevelopment
      value: 'true'
    revisionName: INTERNAL_ADDON|appmeta|_
    scopes:
    - scopeRef:
        apiVersion: apps.abm.io/v1
        kind: Cluster
        name: '{{ Global.CLUSTER_ID }}'
    - scopeRef:
        apiVersion: apps.abm.io/v1
        kind: Namespace
        name: '{{ Global.NAMESPACE_ID }}'
    - scopeRef:
        apiVersion: apps.abm.io/v1
        kind: Stage
        name: '{{ Global.STAGE_ID }}'
  parameterValues:
  - name: CLUSTER_ID
    value: master
  - name: NAMESPACE_ID
    value: ${NAMESPACE_ID}
  - name: STAGE_ID
    value: prod
  - name: APP_ID
    value: search
  - name: ELASTICSEARCH_HOST
    value: ${DATA_ES_HOST}
  - name: ELASTICSEARCH_PORT
    value: ${DATA_ES_PORT}
  - name: REDIS_HOST
    value: '{{ env.APPMANAGER_REDIS_HOST }}'
  - name: REDIS_PORT
    value: '{{ env.APPMANAGER_REDIS_PORT }}'
  - name: REDIS_PASSWORD
    value: '{{ env.APPMANAGER_REDIS_PASSWORD }}'
  - name: ELASTICSEARCH_PASSWORD
    value: ${DATA_ES_PASSWORD}
  - name: DATA_ES_PASSWORD
    value: ${DATA_ES_PASSWORD}
  - name: ELASTICSEARCH_USER
    value: ${DATA_ES_USER}
  - name: DATA_ES_HOST
    value: ${DATA_ES_HOST}
  - name: DATA_ES_PORT
    value: ${DATA_ES_PORT}
  - name: DATA_ES_USER
    value: ${DATA_ES_USER}
  policies: []
  workflow:
    steps: []
