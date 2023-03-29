---
title: 2.2 离线安装
date: 2022-12-27T00:33:55.000Z
toc_max_heading_level: 6
toc_min_heading_level: 2
---

<a name="rleS2"></a>

# 什么是离线安装
由于许多用户的使用环境位于内部网络中，无法直接访问公网上的容器镜像。为了解决这一问题，SREWorks团队对SREWorks的部署机制进行优化，使得其所需容器镜像清单可以自动生成，方便用户快速地将SREWorks移植到内部网络中使用。

<a name="mpsVo"></a>

# 容器镜像清单
以下内容的容器镜像并非一成不变的，会随着版本基线的升级而变化，该版本的镜像清单以根目录下自动生成的`images.txt`为准。当前清单为`v1.4`版本的镜像列表。<br />[https://github.com/alibaba/SREWorks/blob/v1.4/images.txt](https://github.com/alibaba/SREWorks/blob/v1.4/images.txt)
```shell
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/swcli-builtin-package:v1.4
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/kube-rbac-proxy:v0.8.0
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/cluster-clustermanage-db-migration:1672196680474
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/health-health-health:1671710673790
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/system-plugin-account-aliyun-plugin-account-aliyun:1672273106833
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/job-job-master-db-migration:1671709424759
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/redis:v1.0
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-authproxy-db-migration:1672961130368
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/team-team-team:1672186418504
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/app-app-db-migration:1671175450336
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-action-paas-action:1672961130371
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/cluster-clustermanage-clustermanage:1672196680474
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/sw-paas-appmanager-operator:v1.4
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/health-health-db-migration:1671710673790
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/cluster-clustermanage-init-cluster:1672196680474
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-gateway-paas-gateway:1672961130368
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/upload-filemanage-filemanage:1672189294837
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-frontend-route-config:1672961130368
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/system-plugin-aliyun-cluster-plugin-aliyun-cluster:1672273106815
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/dataops-pmdb-db-migration:1672993647228
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/aiops-aisp-sreworks-migration:1672190144467
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-nacos-db-migration:1672961130368
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/search-tkgone-db-migration:1671711750184
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/dataops-warehouse-warehouse:1672993647237
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/kruise-manager:v1.0.1
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/minio:v1.0
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-frontend-paas-frontend:1672961130368
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/sw-progress-check:v1.4
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/kaniko-executor:latest
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/sw-paas-appmanager:v1.4
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/dataops-dataset-dataset:1672993647236
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-action-db-migration:1672961130371
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/dataops-dataset-db-migration:1672993647236
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/dataops-warehouse-db-migration:1672993647237
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/dataops-dataset-db-migration-datasource:1672993647236
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/sw-paas-appmanager-db-migration:v1.4
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/local-path-provisioner:v0.0.23
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/job-job-master-init:1671709424759
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/aiops-processstrategy-processstrategy:1672190144467
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/sw-migrate:v1.4
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-frontend-authproxy-init:1672961130368
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-nacos-paas-nacos:1672961130368
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/sw-paas-appmanager-cluster-init:v1.4
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/mysql:v1.0
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/job-job-master-job-master:1671709424759
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/search-tkgone-tkgone:1671711750184
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/aiops-aisp-aisp:1672190144467
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/team-team-db-migration:1672186418504
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/upload-filemanage-db-migration:1672189294837
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/dataops-pmdb-pmdb:1672993647228
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/zookeeper:v1.0
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/flycore-paas-authproxy-paas-authproxy:1672961130368
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/sw-paas-appmanager-postrun:v1.4
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/sw-postrun:v1.4
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/app-app-app:1671175450336
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/dataops-pmdb-metric-flink:1672993647228
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/aiops-anomalydetection-anomalydetection:1672190144466
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/job-job-worker-job-worker:1671709424759
sreworks-registry.cn-beijing.cr.aliyuncs.com/hub/system-plugin-aliyun-cluster-resource-upload:1672273106815
```
考虑到不同内网环境的差异，容器镜像搬运暂不提供工具，由用户自行完成。如过程中存在问题，可以随时反馈。

<a name="wMgJi"></a>

# 内网SREWorks启动
离线部署的命令示例如下，底层依赖软件和运维应用的镜像仓库需要分开设置：<br />内网镜像仓库以 `sreworks.io/hub-test` 为例：
```shell
# 下载sreworks到本地
git clone http://github.com/alibaba/sreworks.git -b v1.4 sreworks
cd sreworks/chart/sreworks-chart

# 安装SREWorks
helm install sreworks ./ \
    --create-namespace --namespace sreworks \
    --set global.accessMode="nodePort" \
    --set global.images.tag="v1.4" \
    --set appmanager.home.url="http://localhost:30767" \
    --set saas.onlyBase=true \
    --set global.images.registry="sreworks.io/hub-test" \
    --set global.images.imagePullPolicy="IfNotPresent" \
    --set appmanagerbase.kruise.manager.image.repository="sreworks.io/hub-test/kruise-manager" \
    --set appmanagerbase.mysql.image.registry="sreworks.io" \
    --set appmanagerbase.mysql.image.repository="hub-test/mysql" \
    --set appmanagerbase.zookeeper.image.registry="sreworks.io" \
    --set appmanagerbase.zookeeper.image.repository="hub-test/zookeeper" \
    --set appmanagerbase.redis.image.registry="sreworks.io" \
    --set appmanagerbase.redis.image.repository="hub-test/redis" \
    --set appmanagerbase.minio.image.registry="sreworks.io" \
    --set appmanagerbase.minio.image.repository="hub-test/minio" 


```
