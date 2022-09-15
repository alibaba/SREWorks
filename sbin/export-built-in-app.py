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
    "appParameterValues":{
        "REDIS_HOST": '{{ env.APPMANAGER_REDIS_HOST }}',
        "REDIS_PORT": '{{ env.APPMANAGER_REDIS_PORT }}',
        "REDIS_PASSWORD": '{{ env.APPMANAGER_REDIS_PASSWORD }}',
        "ES_ENDPOINT": 'http://${DATA_ES_HOST}:${DATA_ES_PORT}',
        "ES_USERNAME": '${DATA_ES_USER}',
        "ES_PASSWORD": '${DATA_ES_PASSWORD}',
        "DATA_DB_HOST": '${DATAOPS_DB_HOST}',
        "DATA_DB_PORT": '${DATAOPS_DB_PORT}',
        "DATA_DB_USER": '${DATAOPS_DB_USER}',
        "DATA_DB_PASSWORD": '${DATAOPS_DB_PASSWORD}',
        "KAFKA_ENDPOINT": '${KAFKA_ENDPOINT}:9092',
        "ELASTICSEARCH_HOST": '${DATA_ES_HOST}',
        "ELASTICSEARCH_PORT": '${DATA_ES_PORT}',
        "ELASTICSEARCH_USER": '${DATA_ES_USER}',
        "ELASTICSEARCH_PASSWORD": '${DATA_ES_PASSWORD}',
        "DATA_ES_HOST": '${DATA_ES_HOST}',
        "DATA_ES_PORT": '${DATA_ES_PORT}',
        "DATA_ES_USER": '${DATA_ES_USER}',
        "DATA_ES_PASSWORD": '${DATA_ES_PASSWORD}',
        "NAMESPACE_ID": '${NAMESPACE_ID}',
        "CLUSTER_ID": "master",
        "STAGE_ID": "prod",
    },
    "componentParameterValues":{
    }
}


def values_tpl_replace(launchYAML):
    launchYAML['metadata']['annotations']['namespaceId'] = '${NAMESPACE_ID}'
    launchYAML['metadata']['annotations']['clusterId'] = 'master'
    launchYAML['metadata']['annotations']['stageId'] = 'prod'
  
    for appValue in launchYAML["spec"]["parameterValues"]:
        if appValue["name"] in VALUES_MAP["appParameterValues"]:
            appValue["value"] = VALUES_MAP["appParameterValues"][appValue["name"]]
 
    for component in launchYAML["spec"]["components"]:
        newParameterValues = []

        for delKey in ["clusterId","namespaceId","stageId"]:
            if delKey in component:
                del component[delKey]

        for value in component.get("parameterValues", []):
            # 如果该变量在app级别存在，则使用app级别的，且component级别不展示变量
            valueName = value["name"].replace("Global.",'')
            if valueName in VALUES_MAP["appParameterValues"]:
                launchYAML["spec"]["parameterValues"].append({"name": valueName, "value": VALUES_MAP["appParameterValues"][valueName]})
            else:
                newParameterValues.append(value)
        component["parameterValues"] = newParameterValues
        component["scopes"] = [{
           "scopeRef": {
                "apiVersion": "apps.abm.io/v1",
                "kind": "Cluster",
                "name": "{{ Global.CLUSTER_ID }}",
        }},{
           "scopeRef": {
                "apiVersion": "apps.abm.io/v1",
                "kind": "Namespace",
                "name": "{{ Global.NAMESPACE_ID }}",
        }},{
           "scopeRef": {
                "apiVersion": "apps.abm.io/v1",
                "kind": "Stage",
                "name": "{{ Global.STAGE_ID }}",
        }}]
        if component["revisionName"].startswith("INTERNAL_ADDON"):
            component["parameterValues"].append({
                "name": "STAGE_ID",
                "value": "prod",
                "toFieldPaths": ["spec.stageId"]
            })
        if component["revisionName"] == "INTERNAL_ADDON|appmeta|_":
             component["parameterValues"].append({
                "name": "OVERWRITE_IS_DEVELOPMENT",
                "value": "true",
                "toFieldPaths": ["spec.overwriteIsDevelopment"]
            })
            

def only_frontend_filter(launchYAML):

    newComponents = []
    gatewayTrait = {}
    for component in launchYAML["spec"]["components"]:
        if component["revisionName"].startswith("INTERNAL_ADDON"):
            newComponents.append(component)

    launchYAML["spec"]["components"] = newComponents


def only_backend_filter(launchYAML):

    newComponents = []
    gatewayTrait = {}
    for component in launchYAML["spec"]["components"]:
        if not component["revisionName"].startswith("INTERNAL_ADDON"):
            newComponents.append(component)

    launchYAML["spec"]["components"] = newComponents


def frontend_dev_replace(launchYAML, devYAML):
    launchYAML['metadata']['annotations']['stageId'] = 'dev'
    for appValue in launchYAML["spec"]["parameterValues"]:
        if appValue["name"] == "STAGE_ID":
            appValue["value"] = "dev"
    for component in launchYAML["spec"]["components"]:
        for compValue in component["parameterValues"]:
            if compValue["name"] == "STAGE_ID":
                compValue["value"] = "dev"
        if devYAML != None:
            for devComp in devYAML["components"]:
                if devComp["revisionName"] == component["revisionName"]:
                    if "traits" not in component: component["traits"] = []
                    component["traits"] += devComp["traits"]

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
            "parameterValues": [{
                "name": "CLUSTER_ID",
                "value": "",
            },{
                "name": "NAMESPACE_ID",
                "value": "",
            },{
                "name": "STAGE_ID",
                "value": "",
            }],
            "components": [],
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
        else:
            launchYAML["spec"]["components"].append({
                "revisionName": component["componentType"] + "|" + component["componentName"] + "|_"
            })
    
    f = open(loalPath + '/launch-example.yaml', 'w')
    f.write(yaml.safe_dump(launchYAML, width=float("inf")))
    f.close()

    values_tpl_replace(launchYAML)
    f = open(loalPath + '/launch.yaml.tpl', 'w')
    f.write(yaml.safe_dump(launchYAML, width=float("inf")))
    f.close()

    backendLaunchYAML = json.loads(json.dumps(launchYAML))
    only_backend_filter(backendLaunchYAML)
    if len(backendLaunchYAML["spec"]["components"]) > 0:
        f = open(loalPath + '/launch-backend.yaml.tpl', 'w')
        f.write(yaml.safe_dump(backendLaunchYAML, width=float("inf")))
        f.close()

    only_frontend_filter(launchYAML)
    f = open(loalPath + '/launch-frontend.yaml.tpl', 'w')
    f.write(yaml.safe_dump(launchYAML, width=float("inf")))
    f.close()

    if buildIn.get("develop") != None:
        f = open(self_path + "/../" + buildIn["develop"], 'r')
        devYAML = yaml.safe_load(f.read())
        f.close()
    else:
        devYAML = None

    frontend_dev_replace(launchYAML, devYAML)
    f = open(loalPath + '/launch-frontend-dev.yaml.tpl', 'w')
    f.write(yaml.safe_dump(launchYAML, width=float("inf")))
    f.close()


    for name in os.listdir(loalPath):
        filename = loalPath + "/" + name
        if zipfile.is_zipfile(filename):
            dirName = filename + ".dir"
            os.makedirs(dirName)
            with zipfile.ZipFile(filename,"r") as zip_ref:
                zip_ref.extractall(dirName)
            os.remove(filename)


