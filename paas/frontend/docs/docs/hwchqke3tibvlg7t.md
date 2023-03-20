---
title: 2.3.2 在K3s下安装SREWorks
date: 2022-12-14T22:30:54.000Z
toc_max_heading_level: 6
toc_min_heading_level: 2
---

<a name="tm1OY"></a>

# 什么是K3s
K3s 是一款由Rancher Labs发布轻量级的 Kubernetes 发行版，它针对边缘计算、物联网等场景进行了高度优化。<br />K3s主要用于部署在资源受限的边缘计算场景中，也可以在本地运行以进行Kubernetes测试或开发。<br />K3s是为生产环境设计的，因此在PC或笔记本电脑上，K3s是与生产级服务器最接近的选项。<br />k3s 将安装 Kubernetes 所需的一切打包进一个小型二进制文件中，并且完全实现了 Kubernetes API。<br />可以在短时间内安装，并且通常不到 2 分钟的时间就能够启动一个带有几个节点的 k3s 集群，<br />为了减少运行 Kubernetes 所需的内存，Rancher 删除了很多不必要的驱动程序，并用附加组件对其进行替换。

<a name="nUnHA"></a>

# K3s部署
鉴于在笔记本上环境存在一定差异，推荐使用[GitHub - k3d-io/k3d: Little helper to run CNCF’s k3s in Docker](https://github.com/k3d-io/k3d) 来运行。<br />在MAC下执行k3d的安装命令即可快速开始使用
```bash
brew install k3d
```

执行如下命令可以启动一个k3s集群，-p参数用来暴露NodePort的访问端口，使本地浏览器能访问到server上的NodePort
```bash
k3d cluster create -p "30000-32767:30000-32767@server[0]"
```

<a name="TEsDw"></a>

# SREWorks安装
使用K3s自带的`storageClass: local-path`作为存储，关闭默认存储。
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
    --set global.storageClass="local-path" \
    --set saas.onlyBase=true \
    --set localPathProvisioner=false
```

访问 `http://localhost:30767` 即可开始使用SREWorks。
