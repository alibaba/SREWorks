# coding: utf-8

import json
import logging
import os
import sys
import hashlib
import time

import requests

from oauthlib.oauth2 import LegacyApplicationClient
from requests_oauthlib import OAuth2Session

logger = logging.getLogger()
logger.setLevel(logging.INFO)

handler = logging.StreamHandler(sys.stdout)
handler.setLevel(logging.INFO)
formatter = logging.Formatter('[%(asctime)s] [%(module)s.%(funcName)s:%(lineno)d] [%(levelname)s] - %(message)s')
handler.setFormatter(formatter)
logger.addHandler(handler)

CURRENT_PATH = os.path.dirname(os.path.abspath(__file__))
ENDPOINT = 'http://' + os.getenv('ENDPOINT_PAAS_APPMANAGER')

CLIENT_ID = os.environ.get('APPMANAGER_CLIENT_ID')
CLIENT_SECRET = os.environ.get('APPMANAGER_CLIENT_SECRET')
USERNAME = os.environ.get('APPMANAGER_ACCESS_ID')
PASSWORD = os.environ.get('APPMANAGER_ACCESS_SECRET')

class AppManagerClient(object):

    def __init__(self, endpoint, client_id, client_secret, username, password):
        os.environ.setdefault('OAUTHLIB_INSECURE_TRANSPORT', '1')
        self._endpoint = endpoint
        self._client_id = client_id
        self._client_secret = client_secret
        self._username = username
        self._password = password
        self._token = self._fetch_token()

    @property
    def client(self):
        return OAuth2Session(self._client_id, token=self._token)

    def _fetch_token(self):
        """
        获取 appmanager access token
        """
        oauth = OAuth2Session(client=LegacyApplicationClient(client_id=CLIENT_ID))
        return oauth.fetch_token(
            token_url=os.path.join(ENDPOINT, 'oauth/token'),
            username=self._username,
            password=self._password,
            client_id=self._client_id,
            client_secret=self._client_secret
        )


def apply(file_name, type_id):
    """
    导入 post_body 对应的 trait 数据
    :return:
    """
    file_path = os.path.join(CURRENT_PATH, 'swapps') + "/" + file_name
    f = open(file_path)
    config = f.read()
    f.close()
    post_body = {
      "appId": "",
      "typeId": type_id,
      "envId": "Namespace:%s::Stage:%s" % ("sreworks", "dev"),
      "config": config,
      "apiVersion": "core.oam.dev/v1alpha2",
      "enabled": True,
    }
    client = AppManagerClient(ENDPOINT, CLIENT_ID, CLIENT_SECRET, USERNAME, PASSWORD).client

    logger.info(post_body)
    response = client.put(ENDPOINT + '/application-configurations', json=post_body)
    response_json = response.json()
    if response_json.get('code') != 200:
        logger.error('import global swapp to appmanager failed, file=%s, response=%s' % (file_name, response.text))
        sys.exit(1)
    logger.info('import global swapp to appmanager success, file=%s, trait=%s' % (file_name, post_body))


def apply_all_swapps():

    swapps = [
       {
         "file": "productopsv2.yaml",
         "type_id": "Type:components::ComponentType:INTERNAL_ADDON::ComponentName:productopsv2"
       },{
         "file": "system-env.yaml",
         "type_id": "Type:components::ComponentType:RESOURCE_ADDON::ComponentName:system-env@system-env"
       }
     ]

    for swapp in swapps:
        apply(swapp["file"], swapp["type_id"])

# def get_passwd_hash(user_name, user_passwd):
#     key = "%(user_name)s%(local_time)s%(passwd)s" % {
#         'user_name': user_name,
#         'local_time': time.strftime('%Y%m%d', time.localtime(time.time())),
#         'passwd': user_passwd }
#     m = hashlib.md5()
#     m.update(key)
#     return m.hexdigest()
#
# def gen_auth_headers():
#     ## 四个header
#     return {
#         "x-auth-app": os.environ.get('ACCOUNT_SUPER_CLIENT_ID'),
#         "x-auth-key": os.environ.get('ACCOUNT_SUPER_CLIENT_SECRET'),
#         "x-auth-user": os.environ.get('ACCOUNT_SUPER_ID'),
#         "x-auth-passwd": get_passwd_hash(os.environ.get('ACCOUNT_SUPER_ID'), os.environ.get('ACCOUNT_SUPER_SECRET_KEY'))
#     }

if __name__ == '__main__':
    if os.getenv("SREWORKS_INIT") == "enable":
        apply_all_swapps()
    else:
        logger.info('no sreworks init')