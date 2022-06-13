# 2.2 源码构建安装

<a name="kliWz"></a>
# 1. SREWorks源码构建
<a name="xPY76"></a>
## 构建环境准备

- Kubernetes 的版本需要大于等于 **1.20**
- 一台安装有 `git / docker / go`命令的机器
- 一个可用于上传构建容器镜像仓库(执行`docker push`推送镜像)
- 源码构建包含通过Pod构建镜像环节，所需服务器资源量大于快速构建方案(3台4核16G)

![](/pictures/1646727574970-7826d0ea-3ab4-4da0-a6cf-3338b178920c.jpeg.png)

<a name="naB3D"></a>
## 拉取 SREWorks 项目源码
```shell
git clone http://github.com/alibaba/sreworks.git -b v1.1 sreworks
cd sreworks
SW_ROOT=$(pwd)
```

<a name="bIQPN"></a>
## 构建 SREWorks 底座容器镜像
在sreworks目录下，直接在本地执行构建脚本：
```shell
./build.sh --target all --build --tag v1.1
```

<a name="us2zd"></a>
## 上传SREWorks到仓库
将构建产物发布上传到镜像仓库，`SW_REPO`变量替换成用户自己准备的容器镜像仓库。
```shell
SW_REPO="your-registry.***.com/sreworks"
docker login --username=sre****s your-registry.***.com
./build.sh --target all --push $SW_REPO --tag v1.1
```

<a name="jiRmc"></a>
# 2. SREWorks部署&构建运维应用容器镜像
 步骤与快速安装大致相同，替换helm install参数， 触发运维应用来自源码的容器镜像构建
```shell
helm install sreworks $SW_ROOT/chart/sreworks-chart \
    --kubeconfig="****" \
    --create-namespace --namespace sreworks \
    --set appmanager.home.url="https://your-website.***.com" \
    --set build.enable=true \
    --set global.images.tag="v1.1" \
    --set global.images.registry=$SW_REPO

```

<a name="jPt3U"></a>
# 3. Helm安装参数清单
如果需要构建完的运维应用上传到自定义容器镜像仓库，请在执行helm安装命令时候传入以下的参数
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
--set source.branch="v1.1"
--set source.repo="https://code.aliyun.com/sreworks_public/mirror.git"

```
<a name="M4cYp"></a>
# 4. 源码构建依赖资源清单
适用于纯内网构建或者部分资源替换场景

<a name="xW7Sg"></a>
## 底座依赖资源参数
在执行 `./build.sh` 命令前传入
```bash
export SW_PYTHON3_IMAGE="python:3.9.12-alpine"
export APK_REPO_DOMAIN="mirrors.tuna.tsinghua.edu.cn"
export PYTHON_PIP="http://mirrors.aliyun.com/pypi/simple"
export MIGRATE_IMAGE="migrate/migrate"
export MAVEN_IMAGE="maven:3.8.3-adoptopenjdk-11"
export HELM_BIN_URL="https://abm-storage.oss-cn-zhangjiakou.aliyuncs.com/lib/helm"
export KUSTOMIZE_BIN_URL="https://abm-storage.oss-cn-zhangjiakou.aliyuncs.com/lib/kustomize"
export MAVEN_SETTINGS_XML="https://sreworks.oss-cn-beijing.aliyuncs.com/resource/settings.xml"
export GOLANG_IMAGE="golang:alpine"
export GOPROXY="https://goproxy.cn"
export GOLANG_BUILD_IMAGE="golang:1.16"
export DISTROLESS_IMAGE="sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/distroless-static:nonroot"
export MINIO_CLIENT_URL="https://sreworks.oss-cn-beijing.aliyuncs.com/bin/mc-linux-amd64"
export SREWORKS_BUILTIN_PACKAGE_URL="https://sreworks.oss-cn-beijing.aliyuncs.com/packages"
```

<a name="ZYXoU"></a>
## 运维应用依赖资源参数
附加到helm的安装参数上

