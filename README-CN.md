# SREWorks 介绍


阿里巴巴云原生大数据运维平台 SREWorks，沉淀了团队近10年经过内部业务锤炼的 SRE 工程实践，秉承“数据化、智能化”运维思想，帮助运维行业更多的从业者采用“数智”思想做好高效运维。


## SREWorks 是什么？

谷歌在2003年提出了一种岗位叫做 SRE (Site Reliability Engineer，站点可靠性工程师)，它是软件工程师和系统管理员的结合，重视运维人员的开发能力，要求运维日常琐事在50%以内，另外50%精力开发自动化工具减少人力需求。

SREWorks 作为阿里云大数据SRE团队对SRE理念的工程实践，专注于以应用为中心的一站式“云原生”、“数智化”运维 SaaS 管理套件，提供企业应用&资源管理及运维开发两大核心能力，帮助企业实现云原生应用&资源的交付运维。

阿里云大数据 SRE 团队天然靠近大数据和AI，对大数据&AI技术非常熟悉，且具有随取随用的大数据&AI算力资源，一直努力践行“数据化”、“智能化”的运维理念，行业里的 DataOps（数据化运维）最早由该团队提出。SREWorks 中有一套端到端的 DataOps 闭环工程化实践，包括标准的运维数仓、数据运维平台、运营中心等。

传统IT运维领域已经有大量优秀的开源运维平台，反观云原生场景，目前还缺乏一些体系化的运维解决方案。随着云原生时代大趋势的到来，阿里云大数据 SRE 团队将SREWorks运维平台开源，希望为运维工程师们提供开箱即用的运维平台。

## SREWorks 有什么优势？

回归到运维领域的需求，无论上层产品和业务形态怎么变化，运维本质上解决的还是“质量、成本、效率、安全”相关需求。SREWorks 用一个运维 SaaS 应用界面来支撑上述需求，同时以“数智”思想为内核驱动 SaaS 能力，具体包括交付、监测、管理、控制、运营、服务六部分。

![image.png](/paas/sw-frontend/docs/pictures/1627890956935-488725a3-68e9-429f-8671-2371a27a8161.png)




# SREWorks 快速部署

## 1. 前提条件

- Kubernetes 的版本需要大于等于 **1.20**
- 硬件：由于内置了Elasticsearch的开源版默认亲和性原因，建议至少3台节点（配置为4 核 CPU，16G 内存），存储需要300G以上空间。


## 2. 安装部署

推荐使用 [Helm](https://helm.sh/) 来安装 SREWorks

### 安装 Helm 3

使用以下命令安装（如果已安装了 Helm 3，可以跳过这一步骤）：
```
# 适用Mac intel芯片
wget "http://sreworks.oss-cn-beijing.aliyuncs.com/bin/helm-darwin-amd64" -O helm

# 适用Linux intel芯片
# wget https://sreworks.oss-cn-beijing.aliyuncs.com/bin/helm-linux-am64 -O helm

chmod +x ./helm
mv ./helm /usr/local/bin/
```

### 方式一: 安装 SREWorks - 采用Ingress方式访问

- SREWorks的部署必须指定ingress的域名，阿里云ACK集群的域名在【基本信息】中可以找到，例如 `http://*.ceea604.cn-huhehaote.alicontainer.com` ，* 部分用户可以自行填写，比如`http://sreworks.c34a60e3c93854680b590b0d5a190310a.cn-zhangjiakou.alicontainer.com`。未使用阿里云ACK集群的也可以自行确定浏览器访问SREWorks控制台的域名，在安装时传入`appmanager.home.url`参数即可

```
# 下载sreworks到本地
git clone http://github.com/alibaba/sreworks.git sreworks
cd sreworks/chart/sreworks-chart

# 安装SREWorks
helm install sreworks ./ \
    --kubeconfig="****" \
    --create-namespace --namespace sreworks \
    --set appmanager.home.url="https://your-website.***.com" \
    --set global.storageClass="alicloud-disk-available"
```



### 方式二: 安装 SREWorks - 采用NodePort方式访问

```
# 下载sreworks到本地
git clone http://github.com/alibaba/sreworks.git sreworks
cd sreworks/chart/sreworks-chart

# 安装SREWorks
helm install sreworks ./ \
    --kubeconfig="****" \
    --create-namespace --namespace sreworks \
    --set global.accessMode="nodePort" \
    --set appmanager.home.url="http://NODE_IP:30767" \
    --set global.storageClass="alicloud-disk-available"
```

## 3. 验证安装
在浏览器中输入上个步骤的域名，如果能够看到页面，说明已经安装完成(大约需等待5分钟左右)。注册并开始使用 SREWorks。默认账号为`admin`，默认密码为`12345678`


## 4. 卸载
```

helm uninstall sreworks -nsreworks
kubectl delete namespace sreworks
# 如果未删除namespace，请删除namespace下被创建的pvc, helm uninstall不会自动删除pvc

```


## 5. 常见问题

- 1. [SREWorks帮助文档](https://www.yuque.com/sreworks-doc/docs)
- 2. [云原生技术公开课](https://edu.aliyun.com/roadmap/cloudnative)
- 3. 需要使用非默认的kubeconfig，请在helm命令中加入`--kubeconfig`指定目标集群的kubeconfig文件的路径
- 4. 如果遇到长时间Pod处于ContainerCreating，请执行 `kubectl describe pod `命令查看Pod的异常事件
   - 异常事件中出现 `InvalidInstanceType.NotSupportDiskCategory`，说明当前的Node不支持挂载这种云盘类型，请在helm命令中加入 `--set global.storageClass="alicloud-disk-essd"` 进行指定，默认为`alicloud-disk-available`

## 6. 快速部署案例

### 案例1: Kubesphere All-in-one 集群部署SREWorks

Kubesphere All-in-one 集群部署参考 [https://kubesphere.io/zh/docs/quick-start/all-in-one-on-linux/](https://kubesphere.io/zh/docs/quick-start/all-in-one-on-linux/)
```
# 下载并安装kk工具
curl -sfL https://get-kk.kubesphere.io | VERSION=v2.0.0 sh -

# 通过kk工具部署k8s集群
./kk create cluster --with-kubernetes v1.21.5 --with-kubesphere v3.2.1

# 下载sreworks到本地
git clone http://github.com/alibaba/sreworks.git sreworks
cd sreworks/chart/sreworks-chart

# 安装SREWorks
helm install sreworks ./ \
    --create-namespace --namespace sreworks \
    --set global.accessMode="nodePort" \
    --set appmanager.home.url="http://NODE_IP:30767" \
    --set global.storageClass="local"
```

# 联系我们
### 微信群
![image.png](/paas/sw-frontend/src/publicMedia/weixin.jpg)
### 钉钉群
![image.png](/paas/sw-frontend/src/publicMedia/ding.jpg)

