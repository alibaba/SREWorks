export default {
    "id": "vuetimeline", 
    "type": "vuetimeline",
    "name": "vuetimeline",
    "title": "elementUI时间线",
    "info": {
      "author": {
        "name": "",
        "url": "",
      },
      "description": "elementUI组件",
      "links": [],
      "logos": {
        "large": "",
        "small": "",
        "fontClass":""
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
      "docs": "<a target='_blank' href='https://element.eleme.cn/#/zh-CN/component/timeline'>组件文档地址</a>",
    },
    "state": "",
    "latestVersion": "1.0",
    "configSchema": {
      "defaults": {
        "type": "vuetimeline",
        "config": {
          "message": "",
          "showIcon": false,
          "alertType": "success",
          "closable": false,
          "closeText": "关闭",
          "icon":"",
          "description": "",
        },
      },
      "schema": {
        "type": "object",
        "properties": {
          "reverse": {
            "description": "时间线顺序",
              "title": "时间线顺序",
              "required": false,
              "type": "string",
              "initValue": true,
              "x-component": "Radio",
              "x-component-props": {
                "options": [{"value": true, "label": "正序"}, {"value": false, "label": "倒序"}],
              },
          },
          "size": {
            "description": "时间线条目大小",
              "title": "size",
              "required": false,
              "type": "string",
              "initValue": 'normal',
              "x-component": "Radio",
              "x-component-props": {
                "options": [{"value": "normal", "label": "normal"}, {"value": "large", "label": "large"}],
              },
          },
        },
      },
      "supportItemToolbar":false,
      "dataMock": {},
    },
    "category": "vue",
  };