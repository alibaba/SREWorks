# 2.2 源码构建安装

<a name="kliWz"></a>
# 1. SREWorks源码构建
<a name="xPY76"></a>
## 构建环境准备

- Kubernetes 的版本需要大于等于 **1.20**
- 一台安装有 `git / docker`命令的机器
- 一个可用于上传构建容器镜像仓库(执行`docker push`推送镜像)
- 源码构建包含通过Pod构建镜像环节，所需服务器资源量比快速构建方案(2台4核8G)要稍多一些。

![](/pictures/1646727574970-7826d0ea-3ab4-4da0-a6cf-3338b178920c.jpeg.png)

<a name="naB3D"></a>
## 拉取 SREWorks 项目源码
```shell
git clone http://gitlab.alibaba-inc.com/sreworks/sreworks.git --recursive --single-branch
cd sreworks
SW_ROOT=$(pwd)
```

<a name="bIQPN"></a>
## 构建 SREWorks 底座容器镜像
在sreworks目录下，直接在本地执行构建脚本：
```shell
./build.sh --target all --build --tag v1.0
```

<a name="us2zd"></a>
## 上传SREWorks到仓库
将构建产物发布上传到镜像仓库，`SW_REPO`变量替换成用户自己准备的容器镜像仓库。
```shell
SW_REPO="registry.cn-zhangjiakou.***.com/sreworks"
docker login --username=sre****s registry.cn-zhangjiakou.***.com
./build.sh --target all --push $SW_REPO --tag v1.0
```

<a name="jiRmc"></a>
# 2. SREWorks部署&构建运维应用容器镜像
 步骤与快速安装大致相同，替换helm install参数， 触发运维应用来自源码的容器镜像构建
```shell
helm install sreworks $SW_ROOT/chart/sreworks-chart \
    --create-namespace --namespace sreworks \
    --set appmanager.home.url="https://sreworks.c38cca9c474484bdc9873f44f733d8bcd.cn-beijing.alicontainer.com" \
    --set build.enable=true

```
追加helm命令可选参数：
```
    --set global.images.registry="registry.cn-zhangjiakou.aliyuncs.com/sreworks"
    --set appmanagerbase.global.storageClass="hostpath"
    --set platformName="SREWorks"
    --set platformLogo="https://sreworks.oss-cn-beijing.aliyuncs.com/logo/demo.png" 
```
icon格式要求（比如：48*48）

<a name="jPt3U"></a>
# 3. Helm安装参数清单

- 1. 如果需要构建完的运维应用上传到自定义容器镜像仓库，请在执行helm安装命令时候传入以下的参数
```
--set global.images.registry="registry.cn-zhangjiakou.aliyuncs.com/sreworks"
--set appmanager.server.docker.account="sreworks"
--set appmanager.server.docker.password="sreworksDocker123q"
--set appmanager.server.docker.registry="registry.cn-zhangjiakou.aliyuncs.com"
--set appmanager.server.docker.namespace="builds"
```
