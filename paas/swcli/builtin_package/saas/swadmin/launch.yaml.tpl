apiVersion: core.oam.dev/v1alpha2
kind: ApplicationConfiguration
spec:
  parameterValues:
    - name: CLUSTER_ID
      value: "master"
    - name: NAMESPACE_ID
      value: "${NAMESPACE_ID}"
    - name: STAGE_ID
      value: "prod"
    - name: ENDPOINT_PAAS_AUTHPROXY
      value: "${CORE_STAGE_ID}-${CORE_APP_ID}-paas-authproxy"
    - name: ENDPOINT_PAAS_PRODUCTOPS
      value: "${CORE_STAGE_ID}-${CORE_APP_ID}-paas-productops"
    - name: ACCOUNT_SUPER_ACCESS_ID
      value: "test-access-id"
    - name: ACCOUNT_SUPER_ACCESS_KEY
      value: "test-access-key"
    - name: ACCOUNT_SUPER_CLIENT_ID
      value: "common"
    - name: ACCOUNT_SUPER_CLIENT_SECRET
      value: "common-9efab2399c7c560b34de477b9aa0a465"
    - name: ACCOUNT_SUPER_ID
      value: "admin"
    - name: ACCOUNT_SUPER_PK
      value: "999999999"
    - name: ACCOUNT_SUPER_SECRET_KEY
      value: "test-super-secret-key"
  components:
    - revisionName: INTERNAL_ADDON|productops|_
      scopes:
        - scopeRef:
            apiVersion: apps.abm.io/v1
            kind: Cluster
            name: "{{ Global.CLUSTER_ID }}"
        - scopeRef:
            apiVersion: apps.abm.io/v1
            kind: Namespace
            name: "{{ Global.NAMESPACE_ID }}"
        - scopeRef:
            apiVersion: apps.abm.io/v1
            kind: Stage
            name: "{{ Global.STAGE_ID }}"
      parameterValues:
        - name: TARGET_ENDPOINT
          value: "http://{{ Global.ENDPOINT_PAAS_PRODUCTOPS }}"
          toFieldPaths:
            - spec.targetEndpoint
        - name: STAGE_ID
          value: "{{ Global.STAGE_ID }}"
          toFieldPaths:
            - spec.stageId
