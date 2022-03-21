componentType: K8S_MICROSERVICE
componentName: job-master
options:
  containers:
    - ports:
        - containerPort: 17001
      name: master
      build:
        imagePush: ${IMAGE_BUILD_ENABLE}
        imagePushRegistry: ${IMAGE_PUSH_REGISTRY}
        args:
          TAG: ack
        dockerfileTemplateArgs:
          JRE11_IMAGE: registry.cn-hangzhou.aliyuncs.com/alisre/openjdk:11.0.10-jre
        dockerfileTemplate: master-Dockerfile
        repoPath: saas/job/api/sreworks-job
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
        repoPath: saas/job/api/sreworks-job
        branch: ${SOURCE_BRANCH}
        repo: ${SOURCE_REPO}
        ciAccount: "${SOURCE_CI_ACCOUNT}"
        ciToken: "${SOURCE_CI_TOKEN}"
    - name: init
      build:
        imagePush: ${IMAGE_BUILD_ENABLE}
        imagePushRegistry: ${IMAGE_PUSH_REGISTRY}
        args:
          TAG: ack
        dockerfileTemplateArgs:
          MIGRATE_IMAGE: ${MIGRATE_IMAGE}
        dockerfileTemplate: Dockerfile-init
        repoPath: saas/job/api/sreworks-job
        branch: ${SOURCE_BRANCH}
        repo: ${SOURCE_REPO}
        ciAccount: "${SOURCE_CI_ACCOUNT}"
        ciToken: "${SOURCE_CI_TOKEN}"

  env:
    - DB_HOST
    - DB_PORT
    - DB_USER
    - DB_PASSWORD
    - DB_NAME
    - REDIS_HOST
    - REDIS_PORT
    - REDIS_DATABASE
    - REDIS_PASSWORD
    - ES_ENDPOINT
    - ES_USERNAME
    - ES_PASSWORD

---

componentType: K8S_MICROSERVICE
componentName: job-worker
options:
  containers:
    - ports:
        - containerPort: 27001
      name: master
      build:
        imagePush: ${IMAGE_BUILD_ENABLE}
        imagePushRegistry: ${IMAGE_PUSH_REGISTRY}
        args:
          TAG: ack
        dockerfileTemplateArgs:
          JRE11_IMAGE: registry.cn-hangzhou.aliyuncs.com/alisre/openjdk:11.0.10-jre
        dockerfileTemplate: worker-Dockerfile
        repoPath: saas/job/api/sreworks-job
        branch: ${SOURCE_BRANCH}
        repo: ${SOURCE_REPO}
        ciAccount: "${SOURCE_CI_ACCOUNT}"
        ciToken: "${SOURCE_CI_TOKEN}"

  env:
    - SREWORKS_JOB_MASTER_ENDPOINT
    - ES_ENDPOINT
    - ES_USERNAME
    - ES_PASSWORD


