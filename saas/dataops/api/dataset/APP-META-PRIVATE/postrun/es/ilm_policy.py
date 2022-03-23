# coding: utf-8

import requests
from common.constant import host

headers = {}

ilm_policy = {
    "metricbeat": {
        "name": "metricbeat",
        "phases": {
            "hot": {
                "min_age": "0ms",
                "actions": {
                    "rollover": {
                        "max_size": "50gb",
                        "max_age": "7d"
                    }
                }
            },
            "delete": {
                "min_age": "0d",
                "actions": {
                    "delete": {}
                }
            }
        }
    },
    "filebeat": {
        "name": "filebeat",
        "phases": {
            "hot": {
                "min_age": "0ms",
                "actions": {
                    "rollover": {
                        "max_size": "50gb",
                        "max_age": "7d"
                    }
                }
            },
            "delete": {
                "min_age": "0d",
                "actions": {
                    "delete": {}
                }
            }
        }
    }
}


def set_ilm_policy():
    url = host["kibana"] + "/api/index_lifecycle_management/policies"
    for policy_name, policy_config in ilm_policy.items():
        r = requests.post(url, headers=headers, json=policy_config)
        if r.status_code == 200:
            print(r.json())

