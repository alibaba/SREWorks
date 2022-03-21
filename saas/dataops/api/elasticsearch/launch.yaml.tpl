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
    - name: COMPONENT_NAME
      value: "elasticsearch"
  components:
  - dataOutputs: []
    revisionName: "HELM|elasticsearch|_"
    traits:
    - name: gateway.trait.abm.io
      runtime: post
      spec:
        path: "/elasticsearch/**"
        servicePort: 9200
        serviceName: "{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-elasticsearch-master.{{ Global.NAMESPACE_ID }}"

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
    - name: "name"
      value: "{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-elasticsearch"
      toFieldPaths:
      - "spec.name"
    - name: "values"
      value:
        clusterName: "{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-elasticsearch"
        image: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/elasticsearch
        imageTag: "7.10.2-with-plugins"
        volumeClaimTemplate:
          accessModes: [ "ReadWriteOnce" ]
          storageClassName: "${GLOBAL_STORAGE_CLASS}"
          resources:
            requests:
              storage: 500Gi

      toFieldPaths:
      - "spec.values"




