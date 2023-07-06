# SREWorks
**Cloud Native DataOps & AIOps Platform**

![license](https://img.shields.io/github/license/alibaba/sreworks)
![size](https://img.shields.io/github/repo-size/alibaba/sreworks)

[documentation](https://www.yuque.com/sreworks-doc/docs)
[website](https://sreworks.opensource.alibaba.com/)
## Introduction

SREWorks is a cloud-based operation and maintenance (O&M) platform developed by the Alibaba Cloud Big Data SRE team using Big Data and AI concepts for efficient O&M work, called DataOps and AIOps. The platform includes application and resource management and O&M development capabilities for delivering and maintaining cloud-native apps. The SRE team uses comprehensive DataOps tools, including standard O&M warehouses, data O&M platforms, and operation centers, to apply the "DataOps and AIOps" principle and achieve end-to-end closed-loop engineering methods in the SREWorks framework.

## Getting Started

### Prerequisites
- Kubernetes 1.20+
- Hardware:
  - Distributed deployment: It is recommended to have at least 3 nodes (configured with a 4-core CPU and 16GB of memory), with storage space of over 300G, and a k8s cluster capable of at least 90 Pods.
  - Single machine full deployment: It is recommended to have at least 8 cores/32G memory/300G storage space.
  - Single machine basic deployment: It is recommended to have at least 4 cores/16G memory/100G storage space.
- Installation steps and duration (taking the full version as an example):
  - Deploying SREWorks on Kubernetes cluster (1-2 minutes)
  - Deploying O&M applications on the SREWorks (5-15 minutes)
  - After installation, SREWorks can be accessed through a browser.

### Installation

1. Add repository

`helm repo add sreworks https://sreworks.oss-cn-beijing.aliyuncs.com/helm/`

2. Choose network mode parameters

**Ingress**
> The deployment of SREWorks in Ingress mode must specify the domain name of ingress.

- Taking Alibaba Cloud ACK cluster as an example, domain names can be found in _Basic Information_, such as `http://*.ceea604.cn-huhehaote.alicontainer.com` and `http://sreworks.c34a60e3c93854680b590b0d5a190310a.cn-zhangjiakou.alicontainer.com`

- Those who do not use the Alibaba Cloud ACK cluster can find the domain name to access the SREWorks console themselves. When installing, just pass it in the `appmanager.home.url` parameter

The deployment parameters for the Ingress network mode are as follows:

`--set appmanager.home.url="https://your-website.***.com"`

**NodePort**
> When running NodePort mode on a server, it is necessary to pay attention to the settings of network security group or firewall of the node, and you need to open the corresponding port (30767)

The deployment parameters for NodePort mode are as follows:
```
--set global.accessMode="nodePort" 
--set appmanager.home.url="http://NODE_IP:30767"
```

3. Installation with helm

The basic version of SREWorks can be started normally on a **single 4-core with 16G memory** machine. The basic deployment only contains basic application, while the default full deployment contains basic application and digital intelligent application. The deployment command of basic version and NodePort mode is as follows:

Replace NODE_IP with the IP which is accessible from a browser
```
helm install sreworks sreworks/sreworks \
    --create-namespace --namespace sreworks \
    --set global.accessMode="nodePort" \
    --set appmanager.home.url="http://NODE_IP:30767" \
    --set saas.onlyBase=true
```

4. Verify installation

Enter the domain name of the previous step in the browser, and if you can see the page, it indicates that the installation is completed (it will take about 5 minutes). 

Register and start using SREWorks with the following account:

- default account: admin 
- default password: 12345678

5. Uninstall 

Please be sure to follow the order below. Kindly note that you should not delete the namespace without executing `helm uninstall`. Or, it will cause various crds to become dirty data and remain in the cluster!

```
Helm uninstall sreworks -nsreworks
Kubectl delete namespace sreworks
```

**For more installation parameters and FAQs, please see the links below:**

- [Quick Install](https://github.com/alibaba/SREWorks/blob/master/paas/frontend/docs/docs/rr5g10.md)
- [Installation from source code](https://github.com/alibaba/SREWorks/blob/master/paas/frontend/docs/docs/ek2tysaxo4d9108i.md)
- [Document](https://www.yuque.com/sreworks-doc/docs/)
- [Online Demo](https://wj.qq.com/s2/10565748/53da/)

## Contributing

We'd love to accept your patches and contributions to SREWorks. Please refer to [CONTRIBUTING](https://github.com/alibaba/SREWorks/blob/master/CONTRIBUTING.md) for a few small guidelines you need to follow.

## Code of Conduct

Contributions to the SREWorks are expected to adhere to our [Code of Conduct](https://github.com/alibaba/SREWorks/blob/master/CODE_OF_CONDUCT.md).
