---
title: 2.3.4 在Docker Desktop下安装SREWorks
date: 2022-12-26T03:05:12.000Z
toc_max_heading_level: 6
toc_min_heading_level: 2
---

<a name="qDcV6"></a>

# 什么是Docker Desktop
Docker Desktop是适用于Mac或Windows环境的易于安装的应用程序。Docker Desktop可与开发工具和语言一起使用，可以便捷地在本地进行容器构建，持续集成和协作。<br />[https://dockerdocs.cn/desktop/](https://dockerdocs.cn/desktop/)
<a name="dZIkU"></a>

# Docker Desktop如何部署Kubernetes集群
在Docker Desktop的设置(Setttings)中，可以非常方便地启用Kubernetes集群。<br />如果启用k8s集群拉取镜像过慢，可以使用 [https://github.com/AliyunContainerService/k8s-for-docker-desktop](https://github.com/AliyunContainerService/k8s-for-docker-desktop) 来加速镜像拉取。<br />在安装完成后，只需要使用 `kubectl config use-context docker-desktop` 命令就能切换到对应的上下文中。

<a name="TEsDw"></a>

# SREWorks安装
使用Docker Desktop自带的`storageClass: hostpath`作为存储，关闭默认的存储类。
```bash
# 下载sreworks到本地
git clone http://github.com/alibaba/sreworks.git -b v1.4 sreworks
cd sreworks/chart/sreworks-chart

# 安装SREWorks
# 使用k3d自带的storageClass: local-path
helm install sreworks ./ \
    --create-namespace --namespace sreworks \
    --set global.accessMode="nodePort" \
    --set global.images.tag="v1.4" \
    --set appmanager.home.url="http://localhost:30767" \
    --set global.storageClass="hostpath" \
    --set saas.onlyBase=true \
    --set localPathProvisioner=false
```
访问 `http://localhost:30767` 即可开始使用SREWorks。
