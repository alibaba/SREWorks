#!/usr/bin/python
# -*- coding: UTF-8 -*-

import os
import os.path
import sys
from subprocess import Popen, PIPE
import yaml
import json

REGISTRY = "sreworks-registry.cn-beijing.cr.aliyuncs.com/hub"

def popen(command):
    child = Popen(command, stdin = PIPE, stdout = PIPE, stderr = PIPE, shell = True)
    out, err = child.communicate()
    ret = child.wait()
    return (ret, out.decode('utf-8').strip(), err.decode('utf-8').strip())


self_path = os.path.split(os.path.realpath(__file__))[0]
helm_bin = self_path + "/../saas/cluster/api/clustermanage/helm"
(ret, out, err) = popen("which helm")
if ret == 0:
    #helm_bin = out.strip().decode('ascii')
    helm_bin = out

# 使用helm命令渲染获取yaml
command = helm_bin + " template sreworks " + self_path + "/../chart/sreworks-chart --set appmanager.server.jwtSecretKey=a123" + " ".join(sys.argv[1:])
sys.stderr.write(command + "\n")

images = set()

(ret, out, err) = popen(command)
if ret != 0:
    print("unable to exec helm template command")
    print(err)
    sys.exit(1)

for raw in out.split("---\n"):
    content = yaml.safe_load(raw)
    if content == None: 
        continue
    spec = content.get("spec",{}).get("template", {}).get("spec", {})

    for container in spec.get("containers",[]):
        if container.get("image") != None:
            images.add(container.get("image"))

    for container in spec.get("initContainers",[]):
        if container.get("image") != None:
            images.add(container.get("image"))


# 从appmanager提取tagVersion
tagVersion = None
for image in images:
    if "sw-paas-appmanager" in image:
        tagVersion = image.split(":")[-1]

# todo: appmanager的环境变量参数中获取后自动生成
images.add(REGISTRY + "/kaniko-executor:latest")

if tagVersion != None:
    images.add(REGISTRY + "/sw-migrate:" + tagVersion)
    images.add(REGISTRY + "/sw-postrun:" + tagVersion)

# 从saas目录的build中获取
f = open(self_path + '/../built-in.json', 'r')
builtInList = json.loads(f.read())
f.close()
for builtIn in builtInList:
    fb = open(self_path + '/../' + builtIn["packagePath"] + "/meta.yaml", 'r')
    content = yaml.safe_load(fb.read())
    fb.close()
    for package in content.get("componentPackages",[]):
        packageExt = yaml.safe_load(package.get("packageExt"))
        containers = packageExt.get("spec",{}).get("workload",{}).get("spec", {}).get("containers",[])
        initContainers = packageExt.get("spec",{}).get("workload",{}).get("spec", {}).get("initContainers",[])
        for c in containers + initContainers:
            images.add(c["image"])

resultImages = list(images)
resultImages.sort()

for image in resultImages:
    #if not image.startswith(REGISTRY):
    #    continue
    print(image)


