export default {
    "id": "OPS_DESKTOP",
    "type": "OPS_DESKTOP",
    "name": "OPS_DESKTOP",
    "title": "桌面管理",
    "info": {
        "author": {
            "name": "",
            "url": "",
        },
        "description": "桌面管理",
        "links": [],
        "logos": {
            "large": "",
            "small": require("./icon.svg"),
            "fontClass":"OPS_DESKTOP"
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
        "docs": "<a target='_blank' href='https://3x.ant.design/components/alert-cn/'>组件文档地址</a>",
    },
    "state": "",
    "latestVersion": "1.0",
    "configSchema": {
        "defaults": {
            "type": "OPS_DESKTOP",
            "config": {
                "businessConfig": {
                },
            },
        },
        "schema": {
            "type": "object",
            "properties": {
                "businessConfig": {
                    "description": "业务字段配置",
                    "title": "业务字段配置",
                    "required": false,
                    "type": "string",
                    "x-component": "JSON"
                }
            },
        },
        "dataMock": {},
    },
    "catgory": "biz",
};