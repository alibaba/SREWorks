apiVersion: core.oam.dev/v1alpha2
kind: ApplicationConfiguration
spec:
  parameterValues:
    - name: CLUSTER_ID
      value: "master"
    - name: NAMESPACE_ID
      value: "${NAMESPACE_ID}"
    - name: STAGE_ID
      value: "${SAAS_STAGE_ID}"
    - name: ABM_CLUSTER
      value: "default-cluster"
    - name: CLOUD_TYPE
      value: "PaaS"
    - name: ENV_TYPE
      value: "PaaS"
    - name: APP_ID
      value: "dataops"
  components:
  - dataOutputs: []
    revisionName: "HELM|skywalking|_"
    traits: []
    dataInputs: []
    scopes:
    - scopeRef:
        apiVersion: "flyadmin.alibaba.com/v1alpha1"
        kind: "Namespace"
        name: "${NAMESPACE_ID}"
    - scopeRef:
        apiVersion: "flyadmin.alibaba.com/v1alpha1"
        kind: "Cluster"
        name: "master"
    - scopeRef:
        apiVersion: "flyadmin.alibaba.com/v1alpha1"
        kind: "Stage"
        name: "${SAAS_STAGE_ID}"
    dependencies: []
    parameterValues:
    - name: "values"
      value:
        oap:
          replicas: 1
          image:
            repository: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/skywalking-oap-server-utc-8
            tag: 8.5.0-es7
          storageType: elasticsearch7
          javaOpts: -Xmx720m -Xms720m

        ui:
          image:
            tag: 8.5.0

        elasticsearch:
          enabled: false
          config:
            host: "${DATA_ES_HOST}"
            port:
              http: ${DATA_ES_PORT}
            user: "${DATA_ES_USER}"
            password: "${DATA_ES_PASSWORD}"

      toFieldPaths:
      - "spec.values"

 

