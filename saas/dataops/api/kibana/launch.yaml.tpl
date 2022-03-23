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
      value: "kibana"
  components:
  - dataOutputs: []
    revisionName: "HELM|kibana|_"
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
        elasticsearchHosts: "http://{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-elasticsearch-master.{{ Global.NAMESPACE_ID }}.svc.cluster.local:9200"

        #kibanaConfig: 
        #   kibana.yml: |
        #     server.defaultRoute: /gateway/dataops-kibana 

        ingress:
          enabled: false
          annotations: {}
          hosts:
            - host: kibana.${DOMAIN_BASE_INGRESS}
              paths:
                - path: /
          tls: []

        image: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/kibana

        resources:
          requests:
            cpu: "200m"
            memory: 512Mi
          limits:
            cpu: "300m"
            memory: 512Mi

      toFieldPaths:
      - "spec.values"




