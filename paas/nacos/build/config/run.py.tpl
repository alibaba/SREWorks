#!/usr/bin/env python
# -*- coding: utf8 -*-


import sys, os
import zipfile
import logging
import traceback
import requests
import shutil

logger = logging.getLogger()
logger.setLevel(logging.DEBUG)

handler = logging.StreamHandler(sys.stdout)
handler.setLevel(logging.DEBUG)
formatter = logging.Formatter(
    '[abm-paas-nacos-init-config] %(asctime)s [%(process)-d{%(threadName)s}] [%(module)s.%(funcName)s:%(lineno)d] [%(levelname)s] - %(message)s')
handler.setFormatter(formatter)
logger.addHandler(handler)

PRIVATE_NACOS_ENDPOINT = "${ENDPOINT_PAAS_NACOS}"
PRIVATE_NACOS_NAMESPACE = "${NACOS_NAMESPACE}"
ENV_UNIQUE= "${REGION}_${IDC_ROOM}"
file_name = "nacos_config_export.zip"
file_path = "./nacos_config_export"
final_zip_file = 'nacos_config_export-final.zip'


def unzip(file_path, dest_dir):
    if os.path.exists(dest_dir):
        shutil.rmtree(dest_dir)

    if not os.path.exists(dest_dir):
        os.mkdir(dest_dir)
    zip_file = zipfile.ZipFile(file_path)
    try:
        zip_file.extractall(path=dest_dir)
    except RuntimeError as e:
        logger.error("failed, message={}", traceback.format_exc())
        raise e


def zip(dest_dir, file_name):
    filelist = []
    if os.path.isfile(dest_dir):
        filelist.append(dest_dir)
    else:
        for root, dirs, files in os.walk(dest_dir):
            for name in files:
                filelist.append(os.path.join(root, name))
    zf = zipfile.ZipFile(file_name, "w", zipfile.zlib.DEFLATED)
    for tar in filelist:
        arcname = tar[len(dest_dir):]
        # print arcname
        zf.write(tar, arcname)
    zf.close()


def get_all_files(dir):
    files_ = []
    list = os.listdir(dir)
    for i in range(0, len(list)):
        path = os.path.join(dir, list[i])
        if os.path.isdir(path):
            files_.extend(get_all_files(path))
        if os.path.isfile(path):
            files_.append(path)
    return files_

def replace_env(origin_content):
    return origin_content.replace('ENV_UNIQUE',  ENV_UNIQUE)

def render_file():
    files = get_all_files(file_path)
    for file in files:
        logger.info("start render file, file=%s", file)
        file_tpl = '%s.tpl' % file
        os.rename(file, file_tpl)
        with open(file_tpl, 'r') as f:
            logger.info("origin content: %s", f.read())
        sh_command='sh ./render.sh %s %s' % (file_tpl, file)
        os.system(sh_command)
        with open(file, 'rw') as f:
            file_content = f.read()
            if file == '.meta.yml':
                file_content = replace_env(file_content)
                f.write(file_content)
            logger.info("render result: %s", file_content)
        if file.endswith('ENV_UNIQUE'):
            final_file_name = replace_env(file)
            os.rename(file, final_file_name)
        os.remove(file_tpl)
        logger.info("==================================================")


def import_nacos():
    url = 'http://%s/nacos/v1/cs/configs?import=true&namespace=%s' % (PRIVATE_NACOS_ENDPOINT, PRIVATE_NACOS_NAMESPACE)
    file = {'file': open(final_zip_file, 'rb')}
    updload_data = {
        'policy': 'OVERWRITE',
        'filename': final_zip_file
    }
    res = requests.post(url, updload_data, files=file)
    if res.status_code == 200:
        logger.info("import config success")
    else:
        logger.info('import config failed, code=%s, message=%s', res.status_code, res.content)
        raise RuntimeError

def nacos_on_ready():
    url = 'http://%s/nacos/readiness' % PRIVATE_NACOS_ENDPOINT
    res = requests.post(url)
    if res.status_code == 200:
        logger.info('nacos on ready....')
    else:
        logger.info('nacos on ready failed. code=%s, message=%s', res.status_code, res.content)
        raise RuntimeError


def main():
    try:
        unzip(file_name, file_path)
        render_file()
        zip(file_path, final_zip_file)
        import_nacos()
        nacos_on_ready()
    except Exception as e:
        logger.error("exec faile, error=%s", traceback.format_exc())
        sys.exit(1)


if __name__ == '__main__':
    main()
