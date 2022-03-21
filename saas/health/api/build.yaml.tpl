componentType: K8S_MICROSERVICE
componentName: health
options:
  containers:
    - ports:
        - containerPort: 7001
      name: server
      build:
        imagePush: ${IMAGE_BUILD_ENABLE}
        imagePushRegistry: ${IMAGE_PUSH_REGISTRY}
        args:
          TAG: ack
        dockerfileTemplateArgs:
          JRE11_IMAGE: registry.cn-hangzhou.aliyuncs.com/alisre/openjdk:11.0.10-jre
        dockerfileTemplate: Dockerfile
        repoPath: saas/health/api/health
        branch: ${SOURCE_BRANCH}
        repo: ${SOURCE_REPO}
        ciAccount: "${SOURCE_CI_ACCOUNT}"
        ciToken: "${SOURCE_CI_TOKEN}"


  initContainers:
    - name: db-migration
      build:
        imagePush: ${IMAGE_BUILD_ENABLE}
        imagePushRegistry: ${IMAGE_PUSH_REGISTRY}
        args:
          TAG: ack
        dockerfileTemplateArgs:
          MIGRATE_IMAGE: ${MIGRATE_IMAGE}
        dockerfileTemplate: Dockerfile-db-migration
        repoPath: saas/health/api/health
        branch: ${SOURCE_BRANCH}
        repo: ${SOURCE_REPO}
        ciAccount: "${SOURCE_CI_ACCOUNT}"
        ciToken: "${SOURCE_CI_TOKEN}"

  env:
    - DATA_DB_HOST
    - DATA_DB_PORT
    - DATA_DB_HEALTH_NAME
    - DATA_DB_USER
    - DATA_DB_PASSWORD
    - KAFKA_ENDPOINT
    - DATA_DB_NAME
