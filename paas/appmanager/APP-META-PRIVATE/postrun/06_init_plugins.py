# coding: utf-8

import json
import logging
import os
import sys
import yaml
import subprocess
import zipfile

import requests
from oauthlib.oauth2 import LegacyApplicationClient
from requests_oauthlib import OAuth2Session

logger = logging.getLogger()
logger.setLevel(logging.INFO)

handler = logging.StreamHandler(sys.stdout)
handler.setLevel(logging.INFO)
formatter = logging.Formatter("[%(asctime)s] [%(module)s.%(funcName)s:%(lineno)d] [%(levelname)s] - %(message)s")
handler.setFormatter(formatter)
logger.addHandler(handler)

CURRENT_PATH = os.path.dirname(os.path.abspath(__file__))
ENDPOINT = "http://" + os.getenv("ENDPOINT_PAAS_APPMANAGER")
CLIENT_ID = os.getenv("APPMANAGER_CLIENT_ID")
CLIENT_SECRET = os.getenv("APPMANAGER_CLIENT_SECRET")
USERNAME = os.getenv("APPMANAGER_ACCESS_ID")
PASSWORD = os.getenv("APPMANAGER_ACCESS_SECRET")
PLUGINS_PACKAGE_PATH = "/app/plugins/plugins-start-standalone/src/main/resources"
DEFINITION = "/definition.yaml"


class AppManagerClient(object):

    def __init__(self, endpoint, client_id, client_secret, username, password):
        os.environ.setdefault("OAUTHLIB_INSECURE_TRANSPORT", "1")
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
            token_url=os.path.join(ENDPOINT, "oauth/token"),
            username=self._username,
            password=self._password,
            client_id=self._client_id,
            client_secret=self._client_secret
        )


def apply(r, plugin, plugin_version, version, plugin_zip_path):
    """
    上传插件及应用插件
    :return:
    """
    files = [("file", (plugin + ".zip", open(plugin_zip_path, "rb")))]
    response = r.post(ENDPOINT + "/plugins", files=files)
    response_json = response.json()
    if response_json.get("code") != 200:
        message = "upload plugin to appmanager failed, name=%s, response=%s" % (plugin, response.text)
        logger.warning(message)
        if "the plugin has been successfully registered and enabled" in response.text:
            return
        raise Exception(message)
    else:
        logger.info("upload plugin to appmanager success, name=%s" % plugin)
    payload = json.dumps({
        "operation": "enable"
    })
    headers = {
        "Content-Type": "application/json"
    }
    url = ENDPOINT + "/plugins/%s/%s/%s/operate" % (plugin, plugin_version, version)
    response = r.put(url, headers=headers, data=payload)
    response_json = response.json()
    if response_json.get("code") != 200:
        message = "operate plugin to appmanager failed, url=%s, response=%s" % (url, response.text)
        logger.error(message)
        raise Exception(message)
    else:
        logger.info("operate plugin to appmanager success, url=%s" % url)


def apply_all_plugins():
    try:
        r = AppManagerClient(ENDPOINT, CLIENT_ID, CLIENT_SECRET, USERNAME, PASSWORD).client
    except Exception as e:
        logger.error("cannot find appmanager client auth info, skip")
        r = requests

    # 加载所有的component
    apply_plugins(r, "components")
    # 加载所有的trait
    apply_plugins(r, "traits")


def apply_plugins(r, plugins_type):
    plugin_type_path = os.path.join(PLUGINS_PACKAGE_PATH, plugins_type)
    try:
        plugin_type_dir = os.listdir(plugin_type_path)
    except Exception as e:
        logger.warning("plugin_type_dir:%s is non existent: error: %s" % (plugin_type_path, e.__str__()))
        return
    # 遍历当前类型所有的plugin
    for plugin in plugin_type_dir:
        plugin_path = os.path.join(plugin_type_path, plugin)
        plugin_dir = os.listdir(plugin_path)
        # 遍历当前plugin下所有版本
        for plugin_version in plugin_dir:
            plugin_version_path = os.path.join(plugin_path, plugin_version)
            try:
                with open(plugin_version_path + DEFINITION, "rb") as _f:
                    content = _f.read()
                    definition_content = yaml.load(content, Loader=yaml.UnsafeLoader)
                version = definition_content.get("metadata").get("annotations").get("definition.oam.dev/version")
                plugin_zip_path = os.path.join(plugin_version_path, plugin) + ".zip"
                zipDir(plugin_version_path, plugin_zip_path)
                apply(r, plugin, plugin_version, version, plugin_zip_path)
            except Exception as e:
                logger.error("apply plugin to appmanager failed name=%s skip e=%s" % (plugin + plugin_version, e.__str__()))


def zipDir(dirpath, outFullName):
    """
    压缩指定文件夹
    :param dirpath: 目标文件夹路径
    :param outFullName: 压缩文件保存路径+xxxx.zip
    :return: 无
    """
    zip = zipfile.ZipFile(outFullName, "w", zipfile.ZIP_DEFLATED)
    for path, dirnames, filenames in os.walk(dirpath):
        # 去掉目标跟路径，只对目标文件夹下边的文件及文件夹进行压缩
        fpath = path.replace(dirpath, '')

        for filename in filenames:
            zip.write(os.path.join(path, filename), os.path.join(fpath, filename))
    zip.close()

if __name__ == "__main__":
    apply_all_plugins()
