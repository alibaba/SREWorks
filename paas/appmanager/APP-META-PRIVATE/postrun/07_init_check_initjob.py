# coding: utf-8

import logging
import os

import sys
import time
import kubernetes


logger = logging.getLogger()
logger.setLevel(logging.INFO)

handler = logging.StreamHandler(sys.stdout)
handler.setLevel(logging.INFO)
formatter = logging.Formatter('[%(asctime)s] [%(module)s.%(funcName)s:%(lineno)d] [%(levelname)s] - %(message)s')
handler.setFormatter(formatter)
logger.addHandler(handler)


def main():
    """
    appmanager已经部署成功，但是initjob未正确创建
    """
    paas2 = os.getenv("PAAS_VERSION")
    if paas2:
        return
    namespace = "apsara-bigdata-manager"
    initjob = "abm-appmanager-initjob"
    version = "v1"
    plural = "appmanager"
    name = "appmanager"
    group = "abm.io"

    success = False
    kubernetes.config.load_incluster_config()
    job_api = kubernetes.client.BatchV1Api()
    api = kubernetes.client.CustomObjectsApi()
    body = {
        "metadata": {
            "labels": {
                "a": str(int(time.time()*1000))}
        }
    }
    time.sleep(60)
    while True:
        try:
            success = job_api.read_namespaced_job_status(initjob, namespace)
        except Exception as e:
            logger.error("%s find fail, error=%s" % (initjob, e.__str__()))
        if success:
            logger.info("%s already create" % initjob)
            return
        result = api.patch_namespaced_custom_object(group, version, namespace, plural, name, body)
        logger.info("patch %s result=%s" % (name, result))
        time.sleep(600)

if __name__ == '__main__':
    main()
