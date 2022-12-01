---
title: 2.2 安装参数明细
date: 2022-03-25T03:39:17.000Z
toc_max_heading_level: 6
toc_min_heading_level: 2
---

```shell
# 平台名称
--set platformName="SREWorks"

# 平台图标, icon格式要求（比如：48*48）
--set platformLogo="https://sreworks.oss-cn-beijing.aliyuncs.com/logo/demo.png" 

# 底层存储
--set global.storageClass="alicloud-disk-available"

# SREWorks平台启动使用的容器镜像仓库
--set global.images.registry="registry.cn-zhangjiakou.aliyuncs.com/sreworks"

# SaaS容器构建镜像仓库配置
--set appmanager.server.docker.account="sreworks"
--set appmanager.server.docker.password="***"
--set appmanager.server.docker.registry="registry.cn-zhangjiakou.aliyuncs.com"
--set appmanager.server.docker.namespace="builds"

# 源码构建模式的源码仓库来源
--set source.branch="master"
--set source.repo="https://code.aliyun.com/sreworks_public/mirror.git"


# 替换基础应用的主MySQL数据库
# MySQL这块需要注意，通常将主MySQL数据库和数智化MySQL数据库(吞吐较大)分成两个

--set appmanager.server.database.host="*.mysql.rds.aliyuncs.com" 
--set appmanager.server.database.password="****"
--set appmanager.server.database.user="root"
--set appmanager.server.database.port=3306
--set appmanagerbase.mysql.enabled=false

# 替换数智化应用的MySQL数据库
--set saas.dataops.dbHost="*.mysql.rds.aliyuncs.com"
--set saas.dataops.dbUser=root
--set saas.dataops.dbPassword="*****"
--set saas.dataops.dbPort=3306

# 替换数智化应用的ElasticSearch
--set saas.dataops.esHost="*.public.elasticsearch.aliyuncs.com"
--set saas.dataops.esPort="9200"
--set saas.dataops.esUser="elastic"
--set saas.dataops.esPassword="*******"

# 数智化应用中采集开关
--set saas.dataops.filebeatEnable=true
--set saas.dataops.metricbeatEnable=true

# 替换基础应用的MinIO存储
--set global.minio.accessKey="*******"
--set global.minio.secretKey="*******"
--set appmanager.server.package.endpoint="minio.*.com:9000"
--set appmanagerbase.minio.enabled=false
```
