---
title: 6.1 部署Flink云原生开源版
date: 2022-03-25T03:39:03.000Z
toc_max_heading_level: 6
toc_min_heading_level: 2
---

该文档描述在SREWorks中，从0到1建立一个Flink云原生应用定义，发布Flink应用到市场，并交付部署Flink应用的完整过程。

<a name="HutKe"></a>

### 1 新建Flink应用
登录SREWorks后，进入“交付/应用管理-应用开发”页面，点击“新建云原生应用”，在滑出的表单中输入Flink应用定义相关信息，完成Flink应用模板定义<br />![image.png](./pictures/1648179543731-7c274522-0974-48d8-97ce-91f976684883.png)

在应用开发模板列表下，点击应用定义名称，进入应用开发页面<br />![image.png](./pictures/1648179543893-646e35b5-c374-43d4-9ffa-315da591bf7f.png)

<a name="lAX7o"></a>

### 2 添加Flink应用组件
进入Flink应用模板后，在应用组件中添加“Helm”组件类型，将Flink中的VVP组件添加进来<br />![image.png](./pictures/1648179544056-45bf7670-1825-4cd2-9d57-fc1566b6d2a4.png)


<a name="U19f2"></a>

### 3 Flink 应用构建
在完成组件添加后，可以在“应用构建”页面进行一键构建，当前一键构建会自动计算版本，创建构建任务后，在构建列表中可查看刚刚提交的构建任务。<br />![image.png](./pictures/1648179544228-9376276c-b67b-4fce-9dea-d0bd5a20cb88.png)<br />构建完成后，即可在构建列表中展示当前构建的应用及组件构建状态，同时可以在操作中一键部署测试和上架市场。

<a name="K8I4C"></a>

### 4 Flink应用测试部署
应用测试实例支持多套部署，并会自动生成全局唯一的实例名，规则为“应用名-uid”。该实例被部署在同名的namespace下。<br />![image.png](./pictures/1648179544422-00b08297-bc16-44f8-bb77-ff1eabba8cbf.png)<br />用户可自行对应用进行测试，测试通过后，可选择一键发布到市场的操作将应用版本发布到市场中。

<a name="iBcye"></a>

### 5 Flink应用发布上架

通过构建列表中指定构建任务的“上架市场”操作完成应用到市场的发布。![image.png](./pictures/1648179544579-fdb766c8-1f14-49cc-a6d7-7e8f683d9560.png)

<a name="Puqg4"></a>

### 7 Flink应用部署
在市场中可以指定应用一键部署，当前默认部署应用的最新版本。![image.png](./pictures/1648179544732-cf64925e-852d-4305-b347-c5d4f635d0e8.png)
<a name="RfvwI"></a>

### 8 Flink应用实例管理
应用实例列表展示当前部署在prod的企业应用生产实例，并提供升级、回滚、卸载操作。<br />![image.png](./pictures/1648179544887-620a7b87-072c-4488-b530-0f4d2bd13e93.png)
