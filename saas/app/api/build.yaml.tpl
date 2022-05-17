componentType: K8S_MICROSERVICE
componentName: app
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
        repoPath: saas/app/api/app
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
        repoPath: saas/app/api/app
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
    - COOKIE_DOMAIN
    - APPMANAGER_PACKAGE_ENDPOINT_PROTOCOL
    - APPMANAGER_PACKAGE_ENDPOINT
    - APPMANAGER_PACKAGE_ACCESS_KEY
    - APPMANAGER_PACKAGE_SECRET_KEY
    - APPMANAGER_ENDPOINT
    - APPMANAGER_USERNAME
    - APPMANAGER_PASSWORD
    - APPMANAGER_CLIENT_ID
    - APPMANAGER_CLIENT_SECRET
    - AUTHPROXY_ENDPOINT

---

componentType: RESOURCE_ADDON
componentName: system-env@system-env
options: {}
