kind: SessionCluster
apiVersion: v1
metadata:
  name: sreworks-session-cluster
  labels:
    env: prod
  namespace: ${VVP_WORK_NS}
spec:
  state: RUNNING
  deploymentTargetName: sreworksDeploymentTarget
  flinkVersion: 1.14
  flinkImageRegistry: sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror
  flinkImageRepository: flink
  flinkImageTag: 1.14.2-stream1-scala_2.12-java8
  numberOfTaskManagers: 5
  resources:
    jobmanager:
      cpu: 2
      memory: 2g
    taskmanager:
      cpu: 16
      memory: 32g
  flinkConfiguration:
    taskmanager.numberOfTaskSlots: 32
  logging:
    loggingProfile: default
    log4jLoggers:
      "": INFO
      org.apache.flink.streaming.examples: DEBUG
  kubernetes:
    pods:
      envVars:
      - name: KEY
        value: VALUE
