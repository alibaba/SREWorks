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


def apply():
    response = requests.post(ENDPOINT + '/flow-manager/upgrade', headers={
        'X-EmpId': 'SYSTEM',
    }, files={'file': open(os.path.join(CURRENT_PATH, 'app-manager-flow.jar'), 'rb')})
    response_json = response.json()
    if response_json.get('code') != 200:
        logger.error('import app-manager-flow jar to appmanager failed, response=%s' % response.text)
        sys.exit(1)
    logger.info('import app-manager-flow jar to appmanager success')


if __name__ == '__main__':
    apply()
