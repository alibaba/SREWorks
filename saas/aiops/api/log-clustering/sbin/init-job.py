#!/usr/bin/python
# -*- coding: UTF-8 -*-

import hashlib
import time
import requests
import zipfile
import os

JOB_ENDPOINT = "http://prod-job-job-master.sreworks"
JOB_NAME = "log-cluster-pattern2"
JOB_FILE = "/app/upload.zip"

def zip_directory(folder_path, zip_path):
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zip_file:
        for root, dirs, files in os.walk(folder_path):
            for file in files:
                # 将文件的相对路径（相对于folder_path而不是其父目录）作为其在zip文件中的路径
                relative_path = os.path.relpath(os.path.join(root, file), folder_path)
                zip_file.write(os.path.join(root, file), relative_path)


res = requests.get("%s/stream-job/getByName/%s" % (JOB_ENDPOINT, JOB_NAME))
result = res.json()
if result["code"] == 404:
  zip_directory("/app/log-clustering-job", JOB_FILE)
  post_data = {
	"name": JOB_NAME,
	"alias": "日志聚类模式提取",
	"jobType": "python",
	"appId": "aiops",
	"executionPoolId": "0"
  }
  post_file = {"file": open(JOB_FILE, 'rb')}
  upload_route_url = '%s/stream-job/import' % (JOB_ENDPOINT)
  print("upload_route_url: %s" % upload_route_url)
  res = requests.post(upload_route_url, data=post_data, files=post_file)
  print(res.content)
else:
  print("job has exist")
