apiVersion: core.oam.dev/v1alpha2
kind: ApplicationConfiguration
metadata:
  annotations:
    appId: job
    clusterId: master
    namespaceId: ${NAMESPACE_ID}
    stageId: prod
  name: job
spec:
  components:
  - clusterId: ''
    dataInputs: []
    dataOutputs: []
    dependencies: []
    namespaceId: ''
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
    stageId: ''
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
  - name: ES_PASSWORD
    value: ${DATA_ES_PASSWORD}
  - name: REDIS_HOST
    value: '{{ env.APPMANAGER_REDIS_HOST }}'
  - name: REDIS_PORT
    value: '{{ env.APPMANAGER_REDIS_PORT }}'
  - name: REDIS_PASSWORD
    value: '{{ env.APPMANAGER_REDIS_PASSWORD }}'
  - name: ES_USERNAME
    value: ${DATA_ES_USER}
  - name: ES_ENDPOINT
    value: http://${DATA_ES_HOST}:${DATA_ES_PORT}
  - name: ES_PASSWORD
    value: ${DATA_ES_PASSWORD}
  - name: ES_ENDPOINT
    value: http://${DATA_ES_HOST}:${DATA_ES_PORT}
  - name: ES_USERNAME
    value: ${DATA_ES_USER}
  policies: []
  workflow:
    steps: []
