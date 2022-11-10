apiVersion: core.oam.dev/v1alpha2
kind: ApplicationConfiguration
metadata:
  annotations:
    appId: dataops
    clusterId: master
    namespaceId: ${NAMESPACE_ID}
    stageId: dev
  name: dataops
spec:
  components:
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: values
      toFieldPaths:
      - spec.values
      value:
        alertmanager:
          enabled: false
        configmapReload:
          alertmanager:
            enabled: false
          prometheus:
            enabled: false
        kubeStateMetrics:
          enabled: false
        nodeExporter:
          enabled: false
        podSecurityPolicy:
          enabled: false
        pushgateway:
          enabled: false
        rbac:
          create: true
        server:
          enabled: true
          persistentVolume:
            accessModes:
            - ReadWriteOnce
            enabled: true
            existingClaim: ''
            mountPath: /data
            size: 20Gi
            storageClass: '{{ Global.STORAGE_CLASS }}'
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
        serviceAccounts:
          alertmanager:
            annotations: {}
            create: true
            name: null
          nodeExporter:
            annotations: {}
            create: true
            name: null
          pushgateway:
            annotations: {}
            create: true
            name: null
          server:
            annotations: {}
            create: true
            name: null
    - name: name
      toFieldPaths:
      - spec.name
      value: '{{ Global.STAGE_ID }}-dataops-prometheus'
    revisionName: HELM|prometheus|_
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
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: values
      toFieldPaths:
      - spec.values
      value:
        clusterHealthCheckEnable: false
        clusterName: '{{ Global.STAGE_ID }}-dataops-elasticsearch'
        esConfig:
          elasticsearch.yml: 'xpack.security.enabled: true

            discovery.type: single-node

            path.data: /usr/share/elasticsearch/data

            '
        extraEnvs:
        - name: cluster.initial_master_nodes
          value: ''
        - name: ELASTIC_PASSWORD
          value: '{{ Global.DATA_ES_PASSWORD }}'
        - name: ELASTIC_USERNAME
          value: '{{ Global.DATA_ES_USER }}'
        image: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/elasticsearch
        imageTag: 7.10.2-with-plugins
        minimumMasterNodes: 1
        replicas: 1
        volumeClaimTemplate:
          accessModes:
          - ReadWriteOnce
          resources:
            requests:
              storage: 100Gi
          storageClassName: '{{ Global.STORAGE_CLASS }}'
    - name: name
      toFieldPaths:
      - spec.name
      value: '{{ Global.STAGE_ID }}-dataops-elasticsearch'
    revisionName: HELM|elasticsearch|_
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
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: values
      toFieldPaths:
      - spec.values
      value:
        extraEnvs:
        - name: NODE_NAME
          valueFrom:
            fieldRef:
              fieldPath: spec.nodeName
        filebeatConfig:
          filebeat.yml: "filebeat.autodiscover:\n  providers:\n    - type: kubernetes\n      node: ${NODE_NAME}\n      resource: pod\n      scope: node\n      templates:\n        - condition:\n            equals:\n              kubernetes.labels.sreworks-telemetry-log: enable\n          config:\n            - type: container\n              paths:\n                - /var/log/containers/*${data.kubernetes.container.id}.log\n              multiline:\n                type: pattern\n                pattern: '^(\\[)?20\\d{2}-(1[0-2]|0?[1-9])-(0?[1-9]|[1-2]\\d|30|31)'\n                negate: true\n                match: after\n              processors:\n                - add_kubernetes_metadata:\n                    host: ${NODE_NAME}\n                    matchers:\n                    - logs_path:\n                        logs_path: \"/var/log/containers/\"\n\nsetup.ilm.enabled: auto\nsetup.ilm.rollover_alias: \"filebeat\"\nsetup.ilm.pattern: \"{now/d}-000001\"\nsetup.template.name: \"filebeat\"\nsetup.template.pattern: \"filebeat-*\"\n\noutput.elasticsearch:\n  hosts: '{{ Global.DATA_ES_HOST }}:{{ Global.DATA_ES_PORT }}'\n  index: \"filebeat-%{+yyyy.MM.dd}\"\n  username: \"{{ Global.DATA_ES_USER }}\"\n  password: \"{{ Global.DATA_ES_PASSWORD }}\""
        hostNetworking: true
        image: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/filebeat
        imageTag: 7.10.2
        labels:
          k8s-app: filebeat
        podAnnotations:
          name: filebeat
    - name: name
      toFieldPaths:
      - spec.name
      value: '{{ Global.STAGE_ID }}-dataops-filebeat'
    revisionName: HELM|filebeat|_
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
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: values
      toFieldPaths:
      - spec.values
      value:
        adminPassword: sreworks123456
        adminUser: admin
        dashboardProviders:
          dashboardproviders.yaml:
            apiVersion: 1
            providers:
            - disableDeletion: false
              editable: true
              folder: sreworks-dataops
              name: flink
              options:
                path: /var/lib/grafana/dashboards/flink
              orgId: 1
              type: file
            - disableDeletion: false
              editable: true
              folder: sreworks-dataops
              name: cost
              options:
                path: /var/lib/grafana/dashboards/cost
              orgId: 1
              type: file
        dashboards:
          cost:
            cost-dashboard:
              file: dashboards/cost-dashboard.json
          flink:
            flink-dashboard:
              file: dashboards/flink-dashboard.json
        datasources:
          datasources.yaml:
            apiVersion: 1
            datasources:
            - access: proxy
              basicAuth: true
              basicAuthPassword: '{{ Global.DATA_ES_PASSWORD }}'
              basicAuthUser: '{{ Global.DATA_ES_USER }}'
              database: '[metricbeat]*'
              isDefault: true
              jsonData:
                esVersion: 70
                interval: Yearly
                timeField: '@timestamp'
              name: elasticsearch-metricbeat
              type: elasticsearch
              url: http://{{ Global.DATA_ES_HOST }}:{{ Global.DATA_ES_PORT }}
            - access: proxy
              basicAuth: true
              basicAuthPassword: '{{ Global.DATA_ES_PASSWORD }}'
              basicAuthUser: '{{ Global.DATA_ES_USER }}'
              database: '[filebeat]*'
              isDefault: false
              jsonData:
                esVersion: 70
                interval: Yearly
                logLevelField: fields.level
                logMessageField: message
                timeField: '@timestamp'
              name: elasticsearch-filebeat
              type: elasticsearch
              url: http://{{ Global.DATA_ES_HOST }}:{{ Global.DATA_ES_PORT }}
            - access: proxy
              httpMethod: POST
              name: dataops-prometheus
              type: prometheus
              url: http://{{ Global.DATA_PROM_HOST}}:{{ Global.DATA_PROM_PORT }}
            - access: proxy
              isDefault: false
              name: dataset
              type: marcusolsson-json-datasource
              url: http://{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-dataset.{{ Global.NAMESPACE_ID }}
        grafana.ini:
          auth.anonymous:
            enabled: false
          auth.basic:
            enabled: false
          auth.proxy:
            auto_sign_up: true
            enable_login_token: false
            enabled: true
            header_name: x-auth-user
            headers: Name:x-auth-user Email:x-auth-email-addr
            ldap_sync_ttl: 60
            sync_ttl: 60
          security:
            allow_embedding: true
          server:
            root_url: /gateway/dataops-grafana/
            serve_from_sub_path: true
        image:
          repository: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/grafana
          tag: 7.5.3
        plugins:
        - marcusolsson-json-datasource
    - name: name
      toFieldPaths:
      - spec.name
      value: '{{ Global.STAGE_ID }}-dataops-grafana'
    revisionName: HELM|grafana|_
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
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: values
      toFieldPaths:
      - spec.values
      value:
        elasticsearchHosts: http://{{ Global.DATA_ES_HOST }}:{{ Global.DATA_ES_PORT }}
        image: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/kibana
        ingress:
          enabled: false
        kibanaConfig:
          kibana.yml: 'elasticsearch.username: {{ Global.DATA_ES_USER }}

            elasticsearch.password: {{ Global.DATA_ES_PASSWORD }}'
        resources:
          limits:
            cpu: 300m
            memory: 512Mi
          requests:
            cpu: 200m
            memory: 512Mi
    - name: name
      toFieldPaths:
      - spec.name
      value: '{{ Global.STAGE_ID }}-dataops-kibana'
    revisionName: HELM|kibana|_
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
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: values
      toFieldPaths:
      - spec.values
      value:
        clusterRoleRules:
        - apiGroups:
          - ''
          resources:
          - nodes
          - namespaces
          - events
          - pods
          verbs:
          - get
          - list
          - watch
        - apiGroups:
          - extensions
          resources:
          - replicasets
          verbs:
          - get
          - list
          - watch
        - apiGroups:
          - apps
          resources:
          - statefulsets
          - deployments
          - replicasets
          verbs:
          - get
          - list
          - watch
        - apiGroups:
          - ''
          resources:
          - nodes/stats
          - nodes
          - services
          - endpoints
          - pods
          verbs:
          - get
          - list
          - watch
        - nonResourceURLs:
          - /metrics
          verbs:
          - get
        - apiGroups:
          - coordination.k8s.io
          resources:
          - leases
          verbs:
          - '*'
        daemonset:
          annotations:
            name: metricbeat
          enabled: true
          extraEnvs:
          - name: ELASTICSEARCH_HOSTS
            value: '{{ Global.STAGE_ID }}-dataops-elasticsearch-master.{{ Global.NAMESPACE_ID }}.svc.cluster.local'
          - name: NODE_NAME
            valueFrom:
              fieldRef:
                fieldPath: spec.nodeName
          - name: NODE_IP
            valueFrom:
              fieldRef:
                fieldPath: status.hostIP
          hostNetworking: true
          labels:
            k8s-app: metricbeat
          metricbeatConfig:
            metricbeat.yml: "metricbeat.modules:\n- module: kubernetes\n  metricsets:\n    - container\n    - node\n    - pod\n    - system\n    - volume\n  period: 1m\n  host: \"${NODE_NAME}\"\n  hosts: [\"https://${NODE_IP}:10250\"]\n  bearer_token_file: /var/run/secrets/kubernetes.io/serviceaccount/token\n  ssl.verification_mode: \"none\"\n  # If using Red Hat OpenShift remove ssl.verification_mode entry and\n  # uncomment these settings:\n  ssl.certificate_authorities:\n    - /var/run/secrets/kubernetes.io/serviceaccount/ca.crt\n  processors:\n  - add_kubernetes_metadata: ~\n- module: kubernetes\n  enabled: true\n  metricsets:\n    - event\n- module: kubernetes\n  metricsets:\n    - proxy\n  period: 1m\n  host: ${NODE_NAME}\n  hosts: [\"localhost:10249\"]\n- module: system\n  period: 1m\n  metricsets:\n    - cpu\n    - load\n    - memory\n    - network\n    - process\n    - process_summary\n  cpu.metrics: [percentages, normalized_percentages]\n  processes: ['.*']\n  process.include_top_n:\n    by_cpu: 5\n    by_memory: 5\n- module: system\n  period: 1m\n  metricsets:\n    - filesystem\n    - fsstat\n  processors:\n  - drop_event.when.regexp:\n      system.filesystem.mount_point: '^/(sys|cgroup|proc|dev|etc|host|lib)($|/)'\n\nmetricbeat.autodiscover:\n  providers:\n    - type: kubernetes\n      scope: cluster\n      node: ${NODE_NAME}\n      resource: service\n      templates:\n        - condition:\n            equals:\n              kubernetes.labels.sreworks-telemetry-metric: enable\n          config:\n            - module: http\n              metricsets:\n                - json\n              period: 1m\n              hosts: [\"http://${data.host}:10080\"]\n              namespace: \"${data.kubernetes.namespace}#${data.kubernetes.service.name}\"\n              path: \"/\"\n              method: \"GET\"\n\n    - type: kubernetes\n      scope: cluster\n      node: ${NODE_NAME}\n      unique: true\n      templates:\n        - config:\n            - module: kubernetes\n              hosts: [\"kubecost-kube-state-metrics.sreworks-client.svc.cluster.local:8080\"]\n              period: 1m\n              add_metadata: true\n              metricsets:\n                - state_node\n                - state_deployment\n                - state_daemonset\n                - state_replicaset\n                - state_pod\n                - state_container\n                - state_cronjob\n                - state_resourcequota\n                - state_statefulset\n                - state_service\n\nprocessors:\n  - add_cloud_metadata:\n\nsetup.ilm.enabled: auto\nsetup.ilm.rollover_alias: \"metricbeat\"\nsetup.ilm.pattern: \"{now/d}-000001\"\nsetup.template.name: \"metricbeat\"\nsetup.template.pattern: \"metricbeat-*\"\n\noutput.elasticsearch:\n  hosts: '${ELASTICSEARCH_HOSTS:{{ Global.STAGE_ID }}-dataops-elasticsearch-master:9200}'\n  index: \"metricbeat-%{+yyyy.MM.dd}\"\n"
          resources:
            limits:
              cpu: 1000m
              memory: 500Mi
            requests:
              cpu: 100m
              memory: 100Mi
        deployment:
          enabled: false
        image: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/metricbeat
        kube_state_metrics:
          enabled: false
        serviceAccount: metricbeat-sa
    - name: name
      toFieldPaths:
      - spec.name
      value: '{{ Global.STAGE_ID }}-dataops-metricbeat'
    revisionName: HELM|metricbeat|_
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
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: values
      toFieldPaths:
      - spec.values
      value:
        auth:
          rootPassword: cb56b5is5e21_c359b42223
        global:
          storageClass: '{{ Global.STORAGE_CLASS }}'
        image:
          registry: sreworks-registry.cn-beijing.cr.aliyuncs.com
          repository: mirror/mysql
          tag: 8.0.22-debian-10-r44
        primary:
          extraFlags: --max-connect-errors=1000 --max_connections=10000
          persistence:
            size: 50Gi
          service:
            type: ClusterIP
        replication:
          enabled: false
    - name: name
      toFieldPaths:
      - spec.name
      value: '{{ Global.STAGE_ID }}-dataops-mysql'
    revisionName: HELM|mysql|_
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
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: values
      toFieldPaths:
      - spec.values
      value:
        elasticsearch:
          config:
            host: '{{ Global.STAGE_ID }}-dataops-elasticsearch-master.{{ Global.NAMESPACE_ID }}.svc.cluster.local'
            password: '{{ Global.DATA_ES_PASSWORD }}'
            port:
              http: 9200
            user: '{{ Global.DATA_ES_USER }}'
          enabled: false
        oap:
          image:
            repository: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/skywalking-oap-server-utc-8
            tag: 9.2.0
          javaOpts: -Xmx1g -Xms1g
          replicas: 1
          storageType: elasticsearch
        ui:
          image:
            repository: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/skywalking-ui:9.2.0
            tag: 9.2.0
    - name: name
      toFieldPaths:
      - spec.name
      value: '{{ Global.STAGE_ID }}-dataops-skywalking'
    revisionName: HELM|skywalking|_
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
  - dependencies:
    - component: RESOURCE_ADDON|system-env@system-env
    parameterValues:
    - name: values
      toFieldPaths:
      - spec.values
      value:
        acceptCommunityEditionLicense: true
        blobStorageCredentials:
          s3:
            accessKeyId: '{{ Global.MINIO_ACCESS_KEY }}'
            secretAccessKey: '{{ Global.MINIO_SECRET_KEY }}'
        persistentVolume:
          accessModes:
          - ReadWriteOnce
          annotations: {}
          enabled: true
          size: 20Gi
          storageClass: '{{ Global.STORAGE_CLASS }}'
          subPath: ''
        vvp:
          blobStorage:
            baseUri: s3://vvp
            s3:
              endpoint: http://sreworks-minio.sreworks:9000
          globalDeploymentDefaults: "spec:\n  state: RUNNING\n  template:\n    spec:\n      resources:\n        jobmanager:\n          cpu: 0.5\n          memory: 1G\n        taskmanager:\n          cpu: 0.5\n          memory: 1G\n      flinkConfiguration:\n        state.backend: filesystem\n        taskmanager.memory.managed.fraction: 0.0 # no managed memory needed for filesystem statebackend\n        high-availability: vvp-kubernetes\n        metrics.reporter.prom.class: org.apache.flink.metrics.prometheus.PrometheusReporter\n        execution.checkpointing.interval: 10s\n        execution.checkpointing.externalized-checkpoint-retention: RETAIN_ON_CANCELLATION\n"
          persistence:
            type: local
          registry: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror
          sqlService:
            pool:
              coreSize: 1
              maxSize: 1
    - name: name
      toFieldPaths:
      - spec.name
      value: '{{ Global.STAGE_ID }}-dataops-ververica-platform'
    revisionName: HELM|ververica-platform|_
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
  - dataInputs: []
    dataOutputs: []
    dependencies: []
    parameterValues:
    - name: STAGE_ID
      toFieldPaths:
      - spec.stageId
      value: dev
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
    traits:
    - name: gateway.trait.abm.io
      runtime: post
      spec:
        path: /dataset/**
        routeId: dev-dataops-dataset-master-sreworks-dataops-dev
        serviceName: prod-dataops-dataset.sreworks-dataops
        servicePort: 80
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
    - fieldPath: '{{ spec.env.DATA_ES_PASSWORD }}'
      name: Global.DATA_ES_PASSWORD
    - fieldPath: '{{ spec.env.DATA_ES_USER }}'
      name: Global.DATA_ES_USER
    - fieldPath: '{{ spec.env.DATA_ES_HOST }}'
      name: Global.DATA_ES_HOST
    - fieldPath: '{{ spec.env.DATA_ES_PORT }}'
      name: Global.DATA_ES_PORT
    - fieldPath: '{{ spec.env.DATA_PROM_HOST }}'
      name: Global.DATA_PROM_HOST
    - fieldPath: '{{ spec.env.DATA_PROM_PORT }}'
      name: Global.DATA_PROM_PORT
    - fieldPath: '{{ spec.env.DATA_DB_PORT }}'
      name: Global.DATA_DB_PORT
    - fieldPath: '{{ spec.env.DATA_DB_HOST }}'
      name: Global.DATA_DB_HOST
    - fieldPath: '{{ spec.env.DATA_DB_USER }}'
      name: Global.DATA_DB_USER
    - fieldPath: '{{ spec.env.DATA_DB_PASSWORD }}'
      name: Global.DATA_DB_PASSWORD
    - fieldPath: '{{ spec.env.KAFKA_ENDPOINT }}'
      name: Global.KAFKA_ENDPOINT
    - fieldPath: '{{ spec.env.MINIO_ENDPOINT }}'
      name: Global.MINIO_ENDPOINT
    - fieldPath: '{{ spec.env.MINIO_ACCESS_KEY }}'
      name: Global.MINIO_ACCESS_KEY
    - fieldPath: '{{ spec.env.MINIO_SECRET_KEY }}'
      name: Global.MINIO_SECRET_KEY
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
      - DATA_ES_PASSWORD
      - DATA_ES_USER
      - DATA_ES_HOST
      - DATA_ES_PORT
      - DATA_PROM_HOST
      - DATA_PROM_PORT
      - DATA_DB_HOST
      - DATA_DB_PORT
      - DATA_DB_USER
      - DATA_DB_PASSWORD
      - KAFKA_ENDPOINT
      - MINIO_ENDPOINT
      - MINIO_ACCESS_KEY
      - MINIO_SECRET_KEY
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
  - parameterValues:
    - name: STAGE_ID
      toFieldPaths:
      - spec.stageId
      value: dev
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
    traits:
    - name: gateway.trait.abm.io
      runtime: post
      spec:
        path: /pmdb/**
        routeId: dev-dataops-pmdb-master-sreworks-dataops-dev
        serviceName: prod-dataops-pmdb.sreworks-dataops
        servicePort: 80
  - parameterValues:
    - name: STAGE_ID
      toFieldPaths:
      - spec.stageId
      value: dev
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
    traits:
    - name: gateway.trait.abm.io
      runtime: post
      spec:
        path: /warehouse/**
        routeId: dev-dataops-warehouse-master-sreworks-dataops-dev
        serviceName: prod-dataops-warehouse.sreworks-dataops
        servicePort: 80
  parameterValues:
  - name: CLUSTER_ID
    value: master
  - name: NAMESPACE_ID
    value: ${NAMESPACE_ID}
  - name: STAGE_ID
    value: dev
  - name: KAFKA_ENDPOINT
    value: ${KAFKA_ENDPOINT}:9092
  - name: DATA_ES_HOST
    value: ${DATA_ES_HOST}
  - name: DATA_ES_PORT
    value: ${DATA_ES_PORT}
  policies: []
  workflow:
    steps: []
