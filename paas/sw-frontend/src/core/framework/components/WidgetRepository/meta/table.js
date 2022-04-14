/**
 * Created by caoshuaibiao on 2021/2/1.
 */
export default  {
    "id": "TABLE",
    "type": "TABLE",
    "name": "TABLE",
    "title": "表格",
    "info": {
        "author": {
            "name": "",
            "url": ""
        },
        "description": "通用表格",
        "links": [],
        "logos": {
            "large": "",
            "small": require('./icons/table.svg'),
            "fontClass":'TABLE'
        },
        "build": {
            "time": "",
            "repo": "",
            "branch": "",
            "hash": ""
        },
        "screenshots": [],
        "updated": "",
        "version": "",
        "docs":"<div><a target='_blank' href=''>table配置</a></div><br />"+
            "<div><a target='_blank' href='https://3x.ant.design/components/table-cn/'>列完整配置见antd Table Column 配置</a></div>"
    },
    "state": "",
    "latestVersion": "1.0",
    "configSchema": {
        "defaults": {
            "config": {
                "api": {
                    "url": "",
                    "paging": false
                },
                "columns": [
                    {
                        "dataIndex": "label",
                        "filters": [
                            {
                                "text": "Joe",
                                "value": "1"
                            },
                            {
                                "text": "Jim",
                                "value": "8"
                            },
                            {
                                "text": "Submenu",
                                "children": [
                                    {
                                        "text": "Green",
                                        "value": "Green"
                                    },
                                    {
                                        "text": "Black",
                                        "value": "Black"
                                    }
                                ],
                                "value": "Submenu"
                            }
                        ],
                        "label": "label"
                    },
                    {
                        "dataIndex": "value",
                        "render": "<a href='$(row.label)'>$(row.value)</a>",
                        "label": "value"
                    },
                    {
                        "defaultSortOrder": "ascend",
                        "dataIndex": "number",
                        "label": "编号"
                    }
                ],
                "title": "表格",
                "size":"small"
            },
            "type": "TABLE"
        },
        "schema": {
            "type": "object",
            "properties": {
                "columns": {
                    "description": "列定义,更多高级设置参考配置文档",
                    "title": "列定义",
                    "required": false,
                    "x-component": "EditTable",
                    "type": "string",
                    "enableScroll":true,
                    "x-component-props": {
                        "columns": [
                            {
                                "editProps": {
                                    "required": false,
                                    "type": 1,
                                    "inputTip": "列头",
                                },
                                "dataIndex": "label",
                                "title": "列头"
                            },
                            {
                                "editProps": {
                                    "required": false,
                                    "inputTip": "值索引",
                                    "type": 1
                                },
                                "dataIndex": "dataIndex",
                                "title": "值索引"
                            },
                            {
                                "editProps": {
                                    "required": false,
                                    "type": 1,
                                    "inputTip": "自定义列渲染内容,$(row.xxx),来获取行数据,支持系统内置render",
                                },
                                "dataIndex": "render",
                                "title": "render",
                                "width": "200",
                                "textWrap": 'word-break',
                            },
                            {
                                "editProps": {
                                    "required": false,
                                    "inputTip": "列宽,支持百分比",
                                    "type": 1
                                },
                                "dataIndex": "width",
                                "title": "列宽"
                            },

                        ]
                    }
                },
                "size": {
                    "description": "设定表格的行高大小",
                    "title": "行高",
                    "required": false,
                    "type": "string",
                    "x-component": "Radio",
                    "x-component-props":{
                        "options":[{"value":"small","label":"small"},{"value":"middle","label":"middle"},{"value":"default","label":"large"}],
                        "defaultValue":"small"
                    }
                },
                "paging": {
                    "description": "设定表格分页,配置分页需要在请求参数中添加分页参数",
                    "title": "分页",
                    "required": false,
                    "type": "string",
                    "x-component": "Radio",
                    "x-component-props":{
                        "options":[{"value":true,"label":"是"},{"value":false,"label":"否"}],
                        "defaultValue":true
                    }
                },
                "emptyText": {
                    "description": "支持空数据自定义文案",
                    "title": "空数据文案",
                    "required": false,
                    "x-component": "Text",
                    "initValue":"",
                    "type": "string",
                  },
            }
        },
        "supportItemToolbar":true,
        "dataMock": {}
    },
    "catgory": "base"
};