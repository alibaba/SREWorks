export default {
    "id": "FluidCharts",
    "type": "FluidCharts",
    "name": "FluidCharts",
    "title": "水波图",
    "info": {
      "author": {
        "name": "",
        "url": "",
      },
      "description": "bizCharts图表库水波图",
      "links": [],
      "logos": {
        "large": "",
        "small": require("./icon.svg"),
        "fontClass":"FluidCharts"
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
      "docs": "<a target='_blank' href='#/help/book/documents/ho617k.html#84-水波图'>组件文档地址</a>",
    },
    "state": "",
    "latestVersion": "1.0",
    "configSchema": {
      "defaults": {
        "type": "FluidCharts",
        "config": {
          "size": "small",
          "header": "",
          "footer": "",
          "content":"内容不必填",
          "bordered": false,
          "split": true,
          "itemLayout": "horizontal",
          "listItem": {
            "avatar": "icon",
            "title": "title",
            "description": "description"
          },
        },
      },
      "schema": {
        "type": "object",
        "properties": {
          "theme": {
            "description": "主题",
            "title": "主题",
            "required": false,
            "type": "string",
            "x-component": "Radio",
            "initValue": "light",
            "x-component-props": {
              "options": [{"value": "light", "label": "本白"}, {"value": "dark", "label": "亮黑"}],
            },
          },
          "height": {
            "description": "高度",
            "title": "高度",
            "required": false,
            "type": "string",
            "x-component": "INPUT_NUMBER",
          },
          "width": {
            "description": "宽度",
            "title": "宽度",
            "required": false,
            "type": "string",
            "x-component": "INPUT_NUMBER",
          },
          "period": {
            "description": "数据刷新周期(毫秒)",
            "title": "刷新周期(毫秒)",
            "required": false,
            "type": "string",
            "x-component": "INPUT_NUMBER",
          },
          "appendPadding": {
            "description": "设置图表的上右下做四个方位的边距间隔，如10,0,0,10以逗号分隔",
            "title": " 边距",
            "required": false,
            "initValue":"10,0,0,10",
            "type": "string",
            "x-component": "Input",
          },
          "chartTitle": {
            "description": "设置水波的标题",
            "title": "标题",
            "required": false,
            "type": "string",
            "x-component": "Input",
            "x-component-props": {
              "placeholder": "请输入标题",
            },
          },
          "minNum": {
            "description": "设置水波图刻度范围最小值",
            "title": "刻度最小值",
            "required": false,
            "type": "string",
            "x-component": "INPUT_NUMBER",
            "validateType": "number",
            "initValue": 0,
            "x-component-props": {
              "placeholder": "请输入最小刻度数值如：0",
            },
          },
          "maxNum": {
            "description": "设置水波图刻度范围最大值",
            "title": "刻度最大值",
            "required": false,
            "type": "string",
            "x-component": "INPUT_NUMBER",
            "validateType": "number",
            "initValue": 10000,
            "x-component-props": {
              "placeholder": "请输入最小刻度数值如：100",
            },
          },
          "describeTitle": {
            "description": "设置水波数字的描述说明",
            "title": "描述",
            "required": false,
            "type": "string",
            "x-component": "Input",
            "x-component-props": {
              "placeholder": "请输入说明，如比例等",
            },
          },
          "fluidColor": {
            "description": "设置水位图颜色",
            "title": "图表颜色",
            "required": false,
            "type": "string",
            "x-component": "COLOR_PICKER"
          },
          "advancedConfig":{
            "description": "图表高级自定义配置，参考bizcharts官方配置",
            "title": "自定义配置",
            "required": false,
            "type": "string",
            "initValue":"function advancedConfig(widgetData){\n  return {}\n}",
            "x-component": "ACEVIEW_JAVASCRIPT"
          }
        },
      },
      "supportItemToolbar":true,
      "dataMock": {
        "description": "水波图应用于展示比例(百分比)",
        "formats":[
          {
            "description":"接收的数据源为一个number类型的数值，如“26%”的比例来源：2600/10000,10000为水波图的默认分母值(最大值-最小值)，这个最大数值可以在水波图的可视化字段配置处修改。",
            "data": 2600
          }
        ]
      }
    },
    "category": "charts",
  };