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
    revisionName: "HELM|grafana|_"
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
        adminUser: admin
        adminPassword: "${GRAFANA_ADMIN_PASSWORD}"
        grafana.ini:
          security:
            allow_embedding: true
          server:
            root_url: /gateway/dataops-grafana/
            serve_from_sub_path: true
          auth.anonymous:
            enabled: true
        image: 
          repository: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/grafana
          tag: 7.5.3
        plugins:
          - marcusolsson-json-datasource
        datasources:
          datasources.yaml:
            apiVersion: 1
            datasources:
            - name: elasticsearch-metricbeat
              type: elasticsearch
              url: http://{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-elasticsearch-master.{{ Global.NAMESPACE_ID }}:9200
              database: "[metricbeat]*"
              access: proxy
              isDefault: true
              jsonData:
                interval: Yearly
                timeField: "@timestamp"
                esVersion: 70
            - name: elasticsearch-filebeat
              type: elasticsearch
              url: http://{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-elasticsearch-master.{{ Global.NAMESPACE_ID }}:9200
              database: "[filebeat]*"
              access: proxy
              isDefault: false
              jsonData:
                interval: Yearly
                timeField: "@timestamp"
                esVersion: 70
                logMessageField: message
                logLevelField: fields.level
            - name: dataset
              type: marcusolsson-json-datasource
              url: http://{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-dataset.{{ Global.NAMESPACE_ID }}
              access: proxy
              isDefault: false
      toFieldPaths:
      - "spec.values"
