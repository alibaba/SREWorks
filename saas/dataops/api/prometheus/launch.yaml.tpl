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
      value: "prometheus"
  components:
  - dataOutputs: []
    revisionName: "HELM|prometheus|_"
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

        rbac:
          create: true

        podSecurityPolicy:
          enabled: false

        serviceAccounts:
          alertmanager:
            create: true
            name:
            annotations: {}
          nodeExporter:
            create: true
            name:
            annotations: {}
          pushgateway:
            create: true
            name:
            annotations: {}
          server:
            create: true
            name:
            annotations: {}

        alertmanager:
          enabled: false

        configmapReload:
          prometheus:
            enabled: false
          alertmanager:
            enabled: false

        kubeStateMetrics:
          enabled: false

        nodeExporter:
          enabled: false


        server:
          enabled: true
          persistentVolume:
            enabled: true
            accessModes:
              - ReadWriteOnce
            storageClass: "${GLOBAL_STORAGE_CLASS}"
            existingClaim: ""
            mountPath: /data
            size: 20Gi

        pushgateway:
          enabled: false

        serverFiles:
          prometheus.yml:
            rule_files:
              - /etc/config/recording_rules.yml
              - /etc/config/alerting_rules.yml

            scrape_configs:
              - job_name: prometheus
                static_configs:
                  - targets:
                    - localhost:9090
              - job_name: kubernetes-pods
                kubernetes_sd_configs:
                - role: pod
                relabel_configs:
                - action: keep
                  regex: true
                  source_labels:
                  - __meta_kubernetes_pod_annotation_prometheus_io_scrape
                - action: replace
                  regex: (.+)
                  source_labels:
                  - __meta_kubernetes_pod_annotation_prometheus_io_path
                  target_label: __metrics_path__
                - action: replace
                  regex: ([^:]+)(?::\d+)?;(\d+)
                  replacement: $1:$2
                  source_labels:
                  - __address__
                  - __meta_kubernetes_pod_annotation_prometheus_io_port
                  target_label: __address__
                - action: labelmap
                  regex: __meta_kubernetes_pod_label_(.+)
                - action: replace
                  source_labels:
                  - __meta_kubernetes_namespace
                  target_label: kubernetes_namespace
                - action: replace
                  source_labels:
                  - __meta_kubernetes_pod_name
                  target_label: kubernetes_pod_name


      toFieldPaths:
      - "spec.values"




