/**
 * Created by wangkaihua on 2021/07/08.
 * 组件的元数据信息定义,包括类型、logo、配置Schema等，是组件的描述信息定义
 */
export default {
  "id": "Grafana",
  "type": "Grafana",
  "name": "Grafana",
  "title": "Grafana",
  "info": {
    "author": {
      "name": "",
      "url": "",
    },
    "description": "",
    "links": [],
    "logos": {
      "large": "",
      "small": require("./icon.svg"),
      "fontClass":"Grafana"
    },
    "build": {
      "time": "",
      "repo": "",
      "branch": "",
      "hash": "",
    },
    "screenshots": [],
    "updated": "",
    "version": "",
    "docs": "",
  },
  "state": "",
  "latestVersion": "1.0",
  "configSchema": {
    "defaults": {
      "type": "Alert",
      "config": {
        "url": "https://tesla.alibaba-inc.com/m3/d/Q3EsbbLiz/teslaliu-cheng-ping-tai-bao-biao?from=1544025600000&orgId=1&to=1544189013000&panelId=1&embedded&submenu",
      },
    },
    "schema": {
      "type": "object",
      "properties": {
        "url": {
          "description": "",
          "title": "Grafana链接地址",
          "required": true,
          "x-component": "Text",
          "type": "string",
        },
      },
    },
    "dataMock": {},
  },
  "catgory": "base",
};