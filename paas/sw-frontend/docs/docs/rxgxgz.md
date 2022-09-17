---
title: 7.3 代码贡献指南
date: 2022-08-15T03:25:05.000Z
---

<a name="uKYHm"></a>

## 分支规范
- 开发分支为`master` [https://github.com/alibaba/sreworks/tree/master](https://github.com/alibaba/sreworks/tree/master)

<a name="yKUiE"></a>

## 前端本地开发
<a name="h0Fj4"></a>

### 环境要求

- Node = 14  如果本机版本不满足要求，可以使用 [nvm](https://github.com/nvm-sh/nvm) 来做node多版本切换
- 本地未设置前端代理：关闭浏览器跨域安全策略，Chrome <= 93 。[其他版本Chrome下载地址](https://google-chrome.en.uptodown.com/mac/versions)

本地工程设置前端代理：<br />修改webpackDevServer.config.js
```
    proxy: {
      "/gateway": {
        target: "targetUrl",
        changeOrigin: true,
        cookieDomainRewrite:"localhost"
      }
    },
```
可根据本地服务部署环境进行选择

<a name="mwcb0"></a>

### 本地运行
前端(sw-frontend)代码目录 [https://github.com/alibaba/SREWorks/tree/master/paas/sw-frontend](https://github.com/alibaba/SREWorks/tree/master/paas/sw-frontend)
<a name="w58Sg"></a>

#### 安装依赖
```
npm config set registry https://registry.npm.taobao.org
cnpm install 
```

<a name="RuDgz"></a>

#### 本地启动
```shell
npm run pre  # 仅首次需要执行
npm start
```

<a name="jwOAZ"></a>

#### 启动浏览器（没有设置前端代理时）
```shell
open -a "Google Chrome" --args --disable-web-security --disable-features=SameSiteByDefaultCookies,CookiesWithoutSameSiteMustBeSecure --user-data-dir="/Users/xxxx/tempData/chrome"
```
