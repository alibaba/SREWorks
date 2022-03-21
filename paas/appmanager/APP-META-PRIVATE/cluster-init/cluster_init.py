# -*- coding:utf-8 -*-

import logging
import os
import sys

import requests

CREATOR = "122592"
HEADERS = {
    'X-EmpId': CREATOR
}
ENDPOINT = "http://" + os.getenv("ENDPOINT_PAAS_APPMANAGER")

formatter = logging.Formatter('[%(asctime)s] - [%(levelname)s] - %(message)s')
logger = logging.getLogger()
logger.setLevel(logging.INFO)
stdout_hdlr = logging.StreamHandler(sys.stdout)
stdout_hdlr.setFormatter(formatter)
logger.addHandler(stdout_hdlr)


def _insert_cluster(cluster, master_url, oauth_token):
    """
    插入集群
    """
    response = requests.post("%s/clusters" % ENDPOINT, headers=HEADERS, json={
        'clusterId': cluster,
        'clusterName': cluster,
        'clusterType': 'kubernetes',
        'clusterConfig': {
            'masterUrl': master_url,
            'oauthToken': oauth_token,
        },
        'masterFlag': True if cluster == 'master' else False,
    })
    if response.json().get('code') == 200:
        logger.info('cluster has inserted||cluster=%s||master_url=%s||oauth_token=%s'
                    % (cluster, master_url, oauth_token))
    else:
        logger.error('cannot insert cluster info, abort||cluster=%s||master_url=%s||oauth_token=%s||response=%s'
                     % (cluster, master_url, oauth_token, response.text))
        sys.exit(1)


def _update_cluster(cluster, master_url, oauth_token):
    """
    更新集群
    """
    response = requests.put("%s/clusters/%s" % (ENDPOINT, cluster), headers=HEADERS, json={
        'clusterName': cluster,
        'clusterType': 'kubernetes',
        'clusterConfig': {
            'masterUrl': master_url,
            'oauthToken': oauth_token,
        },
        'masterFlag': True if cluster == 'master' else False,
    })
    if response.json().get('code') == 200:
        logger.info('cluster has updated||cluster=%s||master_url=%s||oauth_token=%s'
                    % (cluster, master_url, oauth_token))
    else:
        logger.error('cannot update cluster info, abort||cluster=%s||master_url=%s||oauth_token=%s||response=%s'
                     % (cluster, master_url, oauth_token, response.text))
        sys.exit(1)


def init_cluster():
    """
    初始化 appmanager 集群
    :return:
    """
    cluster_mapping = {}
    items = requests.get("%s/clusters" % ENDPOINT, headers=HEADERS).json().get('data', {}).get('items', [])
    for item in items:
        cluster_mapping[item['clusterId']] = {
            'masterUrl': item['clusterConfig']['masterUrl'],
            'oauthToken': item['clusterConfig']['oauthToken'],
        }

    # 获取当前的 masterUrl 和 oauthToken 信息
    master_url = 'https://%s:%s' % (os.getenv('KUBERNETES_SERVICE_HOST'), os.getenv('KUBERNETES_SERVICE_PORT'))
    with open('/run/secrets/kubernetes.io/serviceaccount/token') as f:
        oauth_token = f.read()

    # 根据实际情况进行 cluster 更新
    clusters = ["master"]
    for cluster in clusters:
        if not cluster_mapping.get(cluster):
            _insert_cluster(cluster, master_url, oauth_token)
        elif cluster_mapping[cluster]['masterUrl'] != master_url or cluster_mapping[cluster]['oauthToken'] != oauth_token:
            _update_cluster(cluster, master_url, oauth_token)
        else:
            logger.info('no need to update cluster %s' % cluster)


if __name__ == "__main__":
    init_cluster()
    logger.info("initjob success")
