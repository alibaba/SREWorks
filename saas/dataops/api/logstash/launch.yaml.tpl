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
    revisionName: "HELM|logstash|_"
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
        image: "sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/logstash"
        logstashPipeline:
          logstash.conf: |
            input {
              beats {
                port => 5044
              }
            }
            output {
              kafka {
                bootstrap_servers => "sreworks-kafka.sreworks.svc.cluster.local:9092"
                codec => json
                topic_id => "%{[@metadata][beat]}-%{[@metadata][version]}"
              }
              elasticsearch {
                hosts => ["${ELASTICSEARCH_HOSTS:{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-elasticsearch-master.{{ Global.NAMESPACE_ID }}.svc.cluster.local:9200}"]
                index => "%{[@metadata][beat]}-%{[@metadata][version]}" 
              }
            }

        volumeClaimTemplate:  
          accessModes: [ "ReadWriteOnce" ]
          storageClassName: "${GLOBAL_STORAGE_CLASS}"
          resources:
            requests:
              storage: 50Gi
        
        service:
          type: ClusterIP
          loadBalancerIP: ""
          ports:
            - name: beats
              port: 5044
              protocol: TCP
              targetPort: 5044
            - name: http
              port: 8080
              protocol: TCP
              targetPort: 8080

      toFieldPaths:
      - "spec.values"
