# 2.3 常见问题

<a name="cB4Be"></a>
### 1. 如何填写组件中的HELM社区仓库
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2022/png/2748/1650904677037-7c5838ab-098f-4948-aabe-9b73dc6e305d.png#clientId=u62befdae-b400-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=419&id=uc6a9a00b&margin=%5Bobject%20Object%5D&name=image.png&originHeight=838&originWidth=2208&originalType=binary&ratio=1&rotation=0&showTitle=false&size=299004&status=done&style=none&taskId=u7477c582-7231-4b7d-948a-72eda646290&title=&width=1104)
<a name="pyyIT"></a>
### 2. Appmanager运行报错，无法创建新线程
```
java.lang.OutOfMemoryError: unable to create native thread
```
需要将 /var/lib/kubelet/config.yaml 中的 `podPidsLimit: 1000` 改为 `podPidsLimit: -1`
