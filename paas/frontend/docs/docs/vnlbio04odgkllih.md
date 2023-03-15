---
title: 2.3.1 在minikube下安装SREWorks
date: 2022-12-14T22:29:19.000Z
toc_max_heading_level: 6
toc_min_heading_level: 2
---


<a name="TdyWR"></a>

# 什么是minikube
Minikube 是一种轻量级的Kubernetes 实现，可在本地计算机上创建VM 并部署仅包含一个节点的简单集群。Minikube 可用于Linux ， macOS 和Windows 系统。<br />该方案由Kubernetes官网维护。
<a name="BugOl"></a>

# minikube部署
部署方案参考minikube官网文档 [minikube start](https://minikube.sigs.k8s.io/docs/start/)<br />本文以MAC笔记本为例：
```bash
minikube start --image-mirror-country=cn --cpus=4 --memory=15gb
```

_如果minikube安装过程中网络问题中断，可能会出现脏数据，清理脏数据参考这个_[_Issue_](https://github.com/kubernetes/minikube/issues/4835#issuecomment-532483752)_，利用_`_docker ps_`_命令找到 _`_google_containers/kicbase_`_ 对应 _`_CONTAINER ID_`_，然后执行_`_ docker exec -ti 容器ID sh_`_ 进入到容器内进行清理。_

<a name="TEsDw"></a>

# SREWorks安装
```bash
# 下载sreworks到本地
git clone http://github.com/alibaba/sreworks.git -b v1.4 sreworks
cd sreworks/chart/sreworks-chart

# 安装SREWorks
# 使用minikube自带的storageClass: standard
helm install sreworks ./ \
    --create-namespace --namespace sreworks \
    --set global.accessMode="nodePort" \
    --set global.images.tag="v1.4" \
    --set appmanager.home.url="http://localhost:30767" \
    --set global.storageClass="standard" \
    --set saas.onlyBase=true \
    --set localPathProvisioner=false
```
在15分钟左右的SREWorks启动完成后，使用kubectl命令将服务转发到本地端口，使用浏览器访问
```bash
kubectl port-forward --address 0.0.0.0 -n sreworks service/prod-flycore-paas-frontend 30767:30767
```
在浏览器中访问 `http://localhost:30767` 即可开始使用SREWorks。

<a name="eTOcv"></a>

# 常见问题

- PVC一直处于Pending状态，可以执行 `kubectl get pod -nkube-system` 观察一下 `storage-provisioner` 这个Pod是否运行正常。如果该Pod异常，会导致PVC一直无存储供应。
