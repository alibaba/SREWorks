import json
import os
import requests

#url = "http://prod-dataops-elasticsearch-master.sreworks-dataops:9200"
url = os.environ.get("ES_ENDPOINT")

headers = {"Content-Type": "application/json"}

data = {
    "persistent": {
        "action.auto_create_index": "-sreworks-job*,*"
    }
}

r = requests.put(url + "/_cluster/settings", data=json.dumps(data), headers=headers)

if r.status_code == 200:
    print(r.json())
else:
    print("status_code:%s" % r.status_code)
    print(r.text)

