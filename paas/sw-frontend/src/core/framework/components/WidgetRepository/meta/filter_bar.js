/**
 * Created by caoshuaibiao on 2021/3/2.
 * 过滤项作为一行,一行占满的时候自动换行
 */
export default  {
    "id": "FILTER_BAR",
    "type": "FILTER_BAR",
    "name": "FILTER_BAR",
    "title": "过滤条",
    "info": {
        "author": {
            "name": "",
            "url": ""
        },
        "description": "过滤条,所有过滤项按照行进行排列",
        "links": [],
        "logos": {
            "large": "",
            "small": require('./icons/filter-bar.svg')
        },
        "build": {
            "time": "",
            "repo": "",
            "branch": "",
            "hash": ""
        },
        "screenshots": [],
        "updated": "",
        "version": ""
    },
    "state": "",
    "latestVersion": "1.0",
    "configSchema": {
        "defaults": {
            "type":"FILTER_BAR",
            "config":{
                "title": "过滤条"
            }
        },
        "schema": {},
        "dataMock": {}
    },
    "catgory": "filter"
};