#!/usr/bin/python
# -*- coding: UTF-8 -*-

import os
import os.path
import sys
import json
import urllib
import zipfile
from tempfile import NamedTemporaryFile
import shutil
import yaml
try:
    import urllib.request              
except ImportError:
    pass

self_path = os.path.split(os.path.realpath(__file__))[0]

VALUES_MAP = {
    "REDIS_HOST": '{{ env.APPMANAGER_REDIS_HOST }}',
    "REDIS_PORT": '{{ env.APPMANAGER_REDIS_PORT }}',
    "REDIS_PASSWORD": '{{ env.APPMANAGER_REDIS_PASSWORD }}',
    "NAMESPACE_ID": '${NAMESPACE_ID}',
    "ES_ENDPOINT": 'http://${DATA_ES_HOST}:${DATA_ES_PORT}',
    "ES_USERNAME": '${DATA_ES_USER}',
    "ES_PASSWORD": '${DATA_ES_PASSWORD}', 
}


def values_tpl_replace(launchYAML):
    launchYAML['metadata']['annotations']['namespaceId'] = '${NAMESPACE_ID}'
    launchYAML['metadata']['annotations']['clusterId'] = 'master'
    launchYAML['metadata']['annotations']['stageId'] = 'prod'
    for component in launchYAML["spec"]["components"]:
        for value in component["parameterValues"]:
            if value["name"] in VALUES_MAP:
                value["value"] = VALUES_MAP[value["name"]]
            elif value["name"].replace("Global.",'') in VALUES_MAP:
                value["value"] = VALUES_MAP[value["name"].replace("Global.",'')]

def download(url):
    if hasattr(urllib, "urlretrieve"):
        f = NamedTemporaryFile(delete=False)
        return urllib.urlretrieve(url, f.name)[0]
    else:
        return urllib.request.urlretrieve(url)[0]

f = open(self_path + '/../built-in.json', 'r')
builtInList = json.loads(f.read())
f.close()

# 获取云端市场的索引文件sw-index.json
if len(sys.argv) > 1:
   endpoint = sys.argv[1]
else:
   endpoint = "http://sreworks.oss-cn-beijing.aliyuncs.com/markets"

f = open(download(endpoint + "/sw-index.json"), 'r')
markets = json.loads(f.read())
f.close()

marketPacks = {}
for marketPack in markets["packages"]:
    marketPacks[marketPack["appId"]] = marketPack

for buildIn in builtInList:
    print(buildIn)
    marketPack = marketPacks[buildIn["appId"]]
    lastVersion = marketPack["packageVersions"][-1]
    if marketPack["urls"][lastVersion].startswith("http"):
        lastPackageUrl = marketPack["urls"][lastVersion]
    else:
        lastPackageUrl = endpoint + "/" + urllib.parse.quote(marketPack["urls"][lastVersion])
 

    loalPath = self_path + "/../" + buildIn["packagePath"]
   
    if os.path.exists(loalPath):
        shutil.rmtree(loalPath)
    else:
        os.makedirs(loalPath)

    print(buildIn["appId"] + " -> " + loalPath)
    with zipfile.ZipFile(download(lastPackageUrl),"r") as zip_ref:
        zip_ref.extractall(loalPath)

    # 根据meta.yaml生成 launch-example.yaml
    f = open(loalPath + '/meta.yaml', 'r')
    metaInfo = yaml.safe_load(f.read())
    f.close()

    launchYAML = {
        "apiVersion": "core.oam.dev/v1alpha2",
        "kind": "ApplicationConfiguration",
        "metadata": {
            "annotations": {
                "appId": buildIn["appId"],
            },
            "name": buildIn["appId"],
        },
        "spec":{
            "components": [],
            "parameterValues": [],
            "policies": [],
            "workflow":{
                "steps": []
            }
        }
    }


    for component in metaInfo["componentPackages"]:
        packageOptions = json.loads(component["packageOptions"])
        if "componentConfiguration" in packageOptions:
            launchYAML["spec"]["components"].append(yaml.safe_load(packageOptions["componentConfiguration"]))
    
    f = open(loalPath + '/launch-example.yaml', 'w')
    f.write(yaml.dump(launchYAML))
    f.close()

    values_tpl_replace(launchYAML)
    f = open(loalPath + '/launch.yaml.tpl', 'w')
    f.write(yaml.dump(launchYAML))
    f.close()

    for name in os.listdir(loalPath):
        filename = loalPath + "/" + name
        if zipfile.is_zipfile(filename):
            dirName = filename + ".dir"
            os.makedirs(dirName)
            with zipfile.ZipFile(filename,"r") as zip_ref:
                zip_ref.extractall(dirName)
            os.remove(filename)


