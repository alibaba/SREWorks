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


def apply(post_json):
    """
    导入 post_json 对应的 definition schema 数据
    :return:
    """
    name = post_json['name']
    response = requests.post(ENDPOINT + '/definition-schemas', json=post_json)
    response_json = response.json()
    if response_json.get('code') != 200:
        logger.error('import definition schema to appmanager failed, name=%s, response=%s' % (name, response.text))
        sys.exit(1)
    logger.info('import definition schema to appmanager success, name=%s' % name)


def apply_all_definition_schemas():
    # 读取所有配置
    config_map = {}
    path = os.path.join(CURRENT_PATH, 'definition_schemas')
    for root, dirs, files in os.walk(path, topdown=False):
        for name in files:
            if not name.endswith('.json'):
                continue
            config_map[name.split('.')[0]] = json.loads(open(os.path.join(root, name)).read())

    for name in config_map:
        post_json = config_map[name]
        post_json['jsonSchema'] = json.dumps(post_json['jsonSchema'], ensure_ascii=False)
        apply(post_json)


if __name__ == '__main__':
    apply_all_definition_schemas()
