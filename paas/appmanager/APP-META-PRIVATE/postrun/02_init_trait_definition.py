# coding: utf-8

import json
import logging
import os
import sys

import requests

logger = logging.getLogger()
logger.setLevel(logging.INFO)

handler = logging.StreamHandler(sys.stdout)
handler.setLevel(logging.INFO)
formatter = logging.Formatter('[%(asctime)s] [%(module)s.%(funcName)s:%(lineno)d] [%(levelname)s] - %(message)s')
handler.setFormatter(formatter)
logger.addHandler(handler)

CURRENT_PATH = os.path.dirname(os.path.abspath(__file__))
ENDPOINT = 'http://' + os.getenv('ENDPOINT_PAAS_APPMANAGER')


def apply(post_body):
    """
    导入 post_body 对应的 trait 数据
    :return:
    """
    response = requests.post(ENDPOINT + '/traits', data=post_body)
    response_json = response.json()
    if response_json.get('code') != 200:
        logger.error('import trait to appmanager failed, response=%s' % response.text)
        sys.exit(1)
    logger.info('import trait to appmanager success, trait=%s' % post_body)


def apply_all_traits():
    # 读取所有配置
    config_map = {}
    path = os.path.join(CURRENT_PATH, 'traits')
    for root, dirs, files in os.walk(path, topdown=False):
        for name in files:
            if not name.endswith('.yaml'):
                continue
            config_map[name.split('.')[0]] = open(os.path.join(root, name)).read()

    for name in config_map:
        post_body = config_map[name]
        apply(post_body)


if __name__ == '__main__':
    apply_all_traits()
