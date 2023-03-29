---
title: 2.3.3 在KubeSphere All in One下安装SREWorks
date: 2022-12-14T22:32:46.000Z
toc_max_heading_level: 6
toc_min_heading_level: 2
---


<a name="DiZOm"></a>

# 什么是**KubeSphere** 
KubeSphere 是在 Kubernetes 之上构建的容器云平台。其 [KubeSphere All in One 方案](https://v3-2.docs.kubesphere.io/zh/docs/quick-start/all-in-one-on-linux/)能够快速交付一个包含 KubeSphere 的Kubernetes集群（链接原文中为其完整方案，本文的命令中也包含了这部分命令）。

<a name="nUnHA"></a>

# **KubeSphere All in One**部署
KubeSphere利用KubeKey（kk）这个工具进行部署，命令如下：
```bash
# 下载并安装kk工具
curl -sfL https://get-kk.kubesphere.io | VERSION=v2.0.0 sh -

# 通过kk工具部署k8s集群
./kk create cluster --with-kubernetes v1.21.5 --with-kubesphere v3.2.1

```

<a name="TEsDw"></a>

# SREWorks安装
使用kk方案自带的`storageClass: local`作为存储，关闭默认存储。
```bash
# 下载sreworks到本地
git clone http://github.com/alibaba/sreworks.git -b v1.4 sreworks
cd sreworks/chart/sreworks-chart

# 安装SREWorks
# 替换NODE_IP为某个节点的浏览器可访问IP
helm install sreworks ./ \
    --create-namespace --namespace sreworks \
    --set global.accessMode="nodePort" \
    --set appmanager.home.url="http://NODE_IP:30767" \
    --set global.storageClass="local" \
    --set localPathProvisioner=false
    
```
