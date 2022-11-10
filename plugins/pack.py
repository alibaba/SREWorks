#!/usr/bin/python
# -*- coding: UTF-8 -*-

import sys
import json
import shutil
import os
import yaml
import argparse

self_path = os.path.split(os.path.realpath(__file__))[0]

parser = argparse.ArgumentParser(description='plugin package tool')
parser.add_argument("-f",'--file', type=str, dest="filePath", required=True, help="specify plugin's path")
args = parser.parse_args()

h  = open(args.filePath + "/definition.yaml", 'r')
defYamlRaw = h.read()
h.close()

pluginInfo = yaml.safe_load(defYamlRaw)

pluginFile = "plugin-" + pluginInfo["metadata"]["name"].replace("/","-") + "-" + pluginInfo["metadata"]["annotations"]["definition.oam.dev/version"]

shutil.make_archive(pluginFile, 'zip', os.path.dirname(args.filePath+"/"))

print(pluginFile + ".zip") 

