#!/usr/bin/python
# -*- coding: UTF-8 -*-

import ssl
import json
import os
import argparse

ssl._create_default_https_context = ssl._create_unverified_context

try:
    import urllib.request as request 
    import urllib.request as urllib
except ImportError:
    import urllib2 as request
    import urllib2 as urllib


self_path = os.path.split(os.path.realpath(__file__))[0]

parser = argparse.ArgumentParser(description='yuque documents sync tool')
parser.add_argument('--token', type=str, dest="token", required=True, help="user's token")
parser.add_argument('--namespace', type=str, dest="namespace", required=True, help="document's namespace like: sreworks-doc/test ")
parser.add_argument('--user-agent', type=str, dest="user_agent", default="ali-opensource", required=False, help="user agent in api http header (default: %(default)s)")
parser.add_argument('--endpoint', type=str, dest="endpoint", default="https://www.yuque.com/api/v2", help="yueque endpoint (default: %(default)s)")
parser.add_argument('--docs-path', type=str, dest="local_docs_path", default="./docs", required=False, help="docusaurus's docs path (default: %(default)s)")
parser.add_argument('--sidebars-file', type=str, dest="sidebars_file", default="./sidebars.json", required=False, help="docusaurus's sidebars.json file (default: %(default)s)")
args = parser.parse_args()

settings = {
  "TOKEN": args.token,
  "USER_AGENT": args.user_agent,
  "ENDPOINT": args.endpoint,
  "NAMESPACE": args.namespace,
  "LOCAL_PATH": args.local_docs_path,
  "SIDEBARS_FILE": args.sidebars_file,
}


if not os.path.exists(settings["LOCAL_PATH"] + "/pictures"):
    os.makedirs(settings["LOCAL_PATH"] + "/pictures")

def yuque(uri):
    headers = {
        "User-Agent": settings["USER_AGENT"],
        "X-Auth-Token": settings["TOKEN"],
    }
    res = urllib.urlopen(request.Request(url=settings["ENDPOINT"] + uri, headers=headers)).read()
    return json.loads(res).get("data")

def mdx_body(meta, content):
    meta_content = "---\n"
    meta_content += "title: " + meta["title"] + "\n"
    meta_content += "date: " + meta["created_at"] + "\n"
    meta_content += "---\n\n"

    if '"format":"lakesheet"' in content:
        #content = json.dumps(meta)
        content = settings["ENDPOINT"].split("//")[0]  + "//" + settings["ENDPOINT"].split("//")[1].split("/")[0] + "/" + settings["NAMESPACE"] + "/" + meta["slug"]
        return meta_content + content

    # js相关代码需要多一个换行
    content = content.replace('```json', "```json\n")
    content = content.replace('```javascript', "```js\n")

    # 代码块中的export特别敏感，还没想到好的办法，先切分开
    content = content.replace('export', "ex port")

    # <a name="8plYw"></a>
    # ### 核心场景
    # a标签和标题处于上下行时会出现问题，需要中间加一个空行
    content = content.replace("</a>\n#", "</a>\n\n#")

    # 遍历图片并下载后替换路径
    for r in content.replace('![](', '![image.png](').split(".png]("):
        if not r.startswith("http"): continue
        pic = r.split(")")[0]
        pic_name = pic.split(".png#")[0].split("/")[-1] + ".png"
        command = "wget '%s' -O %s/pictures/%s" % (pic, settings["LOCAL_PATH"], pic_name)
        os.popen(command)
        content = content.replace(pic, "./pictures/" + pic_name)

    return meta_content + content

toc_list = yuque("/repos/" + settings["NAMESPACE"] + "/toc")
toc_map = {}
for toc in toc_list:
    data = {
       "items": [],
       "label": toc["title"],
       "uuid": toc["uuid"],
       "parent_uuid": toc["parent_uuid"],
       "depth": toc["depth"],
       "lark_type": toc["type"],
       "collapsed": True
    }
    
    if toc["type"] == "TITLE":
       data["type"] = "category"
    elif toc["type"] == "DOC":
       data["type"] = "doc"
    
    if toc["slug"] not in ["","#"]:
        data["id"] = toc["slug"]

    toc_map[toc["uuid"]] = data

    if toc["parent_uuid"] != "":
        toc_map[toc["parent_uuid"]]["items"].append(toc_map[toc["uuid"]])

for k,v in toc_map.items():
    del v["uuid"]
    del v["parent_uuid"]
    del v["lark_type"]
    del v["depth"]
    if len(v["items"]) == 0:
        del v["collapsed"]
        del v["items"]
    elif v["type"] == "doc":
        v["type"] = "category"
        del v["id"]

sidebars_data = []
for toc in toc_list:
    if toc["depth"] == 1:
       sidebars_data.append(toc_map[toc["uuid"]])

if settings["SIDEBARS_FILE"] not in ["false", False, "False"]:
    h = open(self_path + "/" + settings["SIDEBARS_FILE"], 'w')
    h.write(json.dumps({"sidebars":["index"] + sidebars_data}, sort_keys=True, indent=4))
    h.close()

for doc in yuque("/repos/" + settings["NAMESPACE"] + "/docs"):
    print(doc)
    content = yuque("/repos/" + settings["NAMESPACE"] + "/docs/" + str(doc.get('id')))
    h = open(self_path + "/" + settings["LOCAL_PATH"] + "/" + doc.get('slug') + ".md", 'w')
    h.write(mdx_body(doc, content['body']))
    h.close()


