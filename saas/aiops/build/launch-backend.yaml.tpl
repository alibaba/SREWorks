apiVersion: core.oam.dev/v1alpha2
kind: ApplicationConfiguration
metadata:
  annotations:
    appId: aiops
    clusterId: master
    namespaceId: ${NAMESPACE_ID}
    stageId: prod
  name: aiops
spec:
  components:
  - dependencies: []
    parameterValues:
    - name: REPLICAS
      toFieldPaths:
      - spec.replicas
      value: 1
    revisionName: K8S_MICROSERVICE|anomalydetection|_
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
    traits:
    - name: service.trait.abm.io
      runtime: post
      spec:
        ports:
        - port: 80
          protocol: TCP
          targetPort: 5000
    - name: gateway.trait.abm.io
      runtime: post
      spec:
        authEnabled: false
        path: /aiops/anomalydetection/**
        serviceName: '{{ Global.STAGE_ID }}-aiops-anomalydetection.{{ Global.NAMESPACE_ID }}'
  - dependencies: []
    parameterValues:
    - name: REPLICAS
      toFieldPaths:
      - spec.replicas
      value: 1
    - name: Global.TASKPLATFORM_QUERY_URL
      value: http://prod-job-job-master.sreworks/jobInstance/get
    - name: Global.TASKPLATFORM_SUBMIT_URL
      value: http://prod-job-job-master.sreworks/job/start
    - name: Global.AISP_URL
      value: http://prod-aiops-aisp
    revisionName: K8S_MICROSERVICE|processstrategy|_
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
    traits:
    - name: service.trait.abm.io
      runtime: post
      spec:
        ports:
        - port: 80
          protocol: TCP
          targetPort: 5000
    - name: gateway.trait.abm.io
      runtime: post
      spec:
        authEnabled: false
        path: /aiops/processstrategy/**
        serviceName: '{{ Global.STAGE_ID }}-aiops-processstrategy.{{ Global.NAMESPACE_ID }}'
  - dependencies: []
    parameterValues:
    - name: REPLICAS
      toFieldPaths:
      - spec.replicas
      value: 1
    - name: Global.DB_PORT
      value: ${DATAOPS_DB_PORT}
    - name: Global.DB_USER
      value: ${DATAOPS_DB_USER}
    - name: Global.DB_PASSWORD
      value: ${DATAOPS_DB_PASSWORD}
    - name: Global.DB_NAME
      value: aiops_aisp
    - name: Global.CACHE_TYPE
      value: local
    - name: Global.DB_HOST
      value: ${DATAOPS_DB_HOST}
    revisionName: K8S_MICROSERVICE|aisp|_
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
    traits:
    - name: service.trait.abm.io
      runtime: post
      spec:
        ports:
        - port: 80
          protocol: TCP
          targetPort: 7001
    - name: gateway.trait.abm.io
      runtime: post
      spec:
        authEnabled: false
        path: /aiops/aisp/**
        serviceName: '{{ Global.STAGE_ID }}-aiops-aisp.{{ Global.NAMESPACE_ID }}'
  - dataInputs: []
    dataOutputs:
    - fieldPath: '{{ spec.env.DB_HOST }}'
      name: Global.DB_HOST
    - fieldPath: '{{ spec.env.DB_PASSWORD }}'
      name: Global.DB_PASSWORD
    - fieldPath: '{{ spec.env.DB_PORT }}'
      name: Global.DB_PORT
    - fieldPath: '{{ spec.env.DB_USER }}'
      name: Global.DB_USER
    - fieldPath: '{{ spec.env.APPMANAGER_ACCESS_ID }}'
      name: Global.APPMANAGER_USERNAME
    - fieldPath: '{{ spec.env.APPMANAGER_ACCESS_SECRET }}'
      name: Global.APPMANAGER_PASSWORD
    - fieldPath: '{{ spec.env.APPMANAGER_CLIENT_ID }}'
      name: Global.APPMANAGER_CLIENT_ID
    - fieldPath: '{{ spec.env.APPMANAGER_CLIENT_SECRET }}'
      name: Global.APPMANAGER_CLIENT_SECRET
    - fieldPath: '{{ spec.env.COOKIE_DOMAIN }}'
      name: Global.COOKIE_DOMAIN
    - fieldPath: '{{ spec.env.APPMANAGER_PACKAGE_ACCESS_KEY }}'
      name: Global.APPMANAGER_PACKAGE_ACCESS_KEY
    - fieldPath: '{{ spec.env.APPMANAGER_PACKAGE_SECRET_KEY }}'
      name: Global.APPMANAGER_PACKAGE_SECRET_KEY
    - fieldPath: '{{ spec.env.APPMANAGER_PACKAGE_ENDPOINT_PROTOCOL }}'
      name: Global.APPMANAGER_PACKAGE_ENDPOINT_PROTOCOL
    - fieldPath: '{{ spec.env.APPMANAGER_PACKAGE_ENDPOINT }}'
      name: Global.APPMANAGER_PACKAGE_ENDPOINT
    - fieldPath: '{{ spec.env.STORAGE_CLASS }}'
      name: Global.STORAGE_CLASS
    - fieldPath: '{{ spec.env.ACCOUNT_SUPER_CLIENT_ID }}'
      name: Global.ACCOUNT_SUPER_CLIENT_ID
    - fieldPath: '{{ spec.env.ACCOUNT_SUPER_CLIENT_SECRET }}'
      name: Global.ACCOUNT_SUPER_CLIENT_SECRET
    dependencies: []
    parameterValues:
    - name: keys
      toFieldPaths:
      - spec.keys
      value:
      - DB_HOST
      - DB_PASSWORD
      - DB_PORT
      - DB_USER
      - APPMANAGER_ACCESS_ID
      - APPMANAGER_ACCESS_SECRET
      - APPMANAGER_CLIENT_ID
      - APPMANAGER_CLIENT_SECRET
      - COOKIE_DOMAIN
      - APPMANAGER_PACKAGE_ACCESS_KEY
      - APPMANAGER_PACKAGE_SECRET_KEY
      - APPMANAGER_PACKAGE_ENDPOINT_PROTOCOL
      - APPMANAGER_PACKAGE_ENDPOINT
      - STORAGE_CLASS
      - ACCOUNT_SUPER_CLIENT_ID
      - ACCOUNT_SUPER_CLIENT_SECRET
    revisionName: RESOURCE_ADDON|system-env@system-env|1.0
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
  parameterValues:
  - name: CLUSTER_ID
    value: master
  - name: NAMESPACE_ID
    value: ${NAMESPACE_ID}
  - name: STAGE_ID
    value: prod
  policies: []
  workflow:
    steps: []
