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


def apply_all_versions():
    post_body = {
        "version": "sreworks,dev",
        "versionLabel": "默认",
    }
    client = AppManagerClient(ENDPOINT, CLIENT_ID, CLIENT_SECRET, USERNAME, PASSWORD).client
    response = client.post(ENDPOINT + '/versions', json=post_body, headers={
        "X-Biz-App": "%s,%s,%s" % ("unknown", "sreworks", "dev")
    })
    response_json = response.json()
    print(response_json)

if __name__ == '__main__':
    if os.getenv("SREWORKS_INIT") == "enable":
        apply_all_versions()
    else:
        logger.info('no sreworks init')