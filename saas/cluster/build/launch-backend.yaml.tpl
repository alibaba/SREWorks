apiVersion: core.oam.dev/v1alpha2
kind: ApplicationConfiguration
metadata:
  annotations:
    appId: cluster
    clusterId: master
    namespaceId: ${NAMESPACE_ID}
    stageId: prod
  name: cluster
spec:
  components:
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: REPLICAS
      toFieldPaths:
      - spec.replicas
      value: 1
    - name: Global.DB_NAME
      value: sreworks_meta
    - name: Global.APPMANAGER_ENDPOINT
      value: http://sreworks-appmanager
    - name: Global.AUTHPROXY_ENDPOINT
      value: http://prod-flycore-paas-authproxy
    revisionName: K8S_MICROSERVICE|clustermanage|_
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
        authEnabled: true
        path: /sreworks/clustermanage/**
        serviceName: '{{ Global.STAGE_ID }}-cluster-clustermanage.{{ Global.NAMESPACE_ID }}'
  - clusterId: ''
    dataInputs: []
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
    namespaceId: ''
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
    stageId: ''
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
