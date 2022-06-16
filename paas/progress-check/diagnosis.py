from kubernetes import client, config
import time

class KubernetesTools(object):
    def __init__(self):
        self.core = client.CoreV1Api()
        self.storage = client.StorageV1Api()
        self.batch = client.BatchV1Api()

    def get_pvc(self, namespace):
        return self.core.list_namespaced_persistent_volume_claim(namespace).items

    def get_storage_class(self):
        return self.storage.list_storage_class().items

    def get_event(self, namespace, apiVersion=None, kind=None, metaname=None):
        events = []
        for item in self.core.list_namespaced_event(namespace).items:
            if apiVersion is not None and item.involved_object.apiVersion != apiVersion:
                continue
            if kind is not None and item.involved_object.kind != kind:
                continue
            if metaname is not None and item.involved_object.name != metaname:
                continue
            events.append(item)
        return events


class Diagnosis(object):
    def __init__(self, ktools: KubernetesTools):
        self.ktools = ktools
        self.pass_list = set()

    def execute(self):
        if "check_pvc" not in self.pass_list:
            message = self.check_pvc()
            if message is None:
                self.pass_list.add("check_pvc")
            else:
                return message

    def check_pvc(self):

        # 场景1. 校验pvc中storageclass是否存在
        pvcs = self.ktools.get_pvc("sreworks")
        storageclass_list = set()
        for pvc in pvcs:
            storageclass_list.add(pvc.spec.storage_class_name)

        check_storageclass_list = set()
        for sc in self.ktools.get_storage_class():
            if sc.metadata.name in storageclass_list:
                check_storageclass_list.add(sc.metadata.name)

        if len(storageclass_list - check_storageclass_list) > 0:
            return "%s not found in StorageClass, Please check helm install parameter --set global.storageClass=??  " % ','.join(
                storageclass_list - check_storageclass_list)

        # 场景2. 校验pvc功能是否可以正常使用
        # 查询pvc的状态和事件 -> 在状态不对的时候查事件
        error_pvc = {"name": None, "status": None}
        for pvc in pvcs:
            if pvc.status.phase != "Bound":
                error_pvc["name"] = pvc.metadata.name
                error_pvc["status"] = pvc.status.phase
                break

        if error_pvc['name'] is not None:
            events = self.ktools.get_event("sreworks", kind="PersistentVolumeClaim", metaname=error_pvc["name"])
            if len(events) > 0:
                return "PersistentVolumeClaim %s status:%s Event:%s" % (
                    error_pvc["name"], error_pvc['status'], events[0].message)
            else:
                return "PersistentVolumeClaim %s status:%s Event not found" % (error_pvc["name"], error_pvc['status'])

    def check_base(self):

        # 场景3. 检查底座软件运行是否正常 MySQL/MinIO

        return "123"

    def check_appmanager(self):

        # 场景3. 检查appmanager运行是否正常,初始化是否完成

        return "123"


def process_check(ktools: KubernetesTools, last_info) -> bool:
    apps = set()
    completed_apps = set()

    for job in ktools.batch.list_namespaced_job(namespace='sreworks').items:
        if not job.metadata.name.endswith("-init-job"):
            continue
        app_name = job.metadata.name.replace('-init-job', '').replace('sreworks-saas-', '').replace('sreworks-', '')
        apps.add(app_name)
        if job.status.succeeded == 1:
            completed_apps.add(app_name)

    message = last_info["diagnosis"].execute()

    stdout = ''
    stdout += 'Progress: %.2f%% \n' % (len(completed_apps) / len(apps) * 100.0)
    stdout += 'Installing: %s \n' % ' | '.join(apps - completed_apps)
    stdout += 'Finished: %s \n' % ' | '.join(completed_apps)
    if message is not None:
        stdout += 'Message: %s \n' % message

    if last_info["stdout"] != stdout:
        print("%sUseTime: %s\n" % (stdout, int(time.time()) - last_info["start_time"]))
        last_info["stdout"] = stdout

    if apps == completed_apps:
        return False

    return True


if __name__ == '__main__':
    # config.load_kube_config()        # 在服务器上使用该方法
    config.load_incluster_config()       # 在pod中用该方法
    ktools = KubernetesTools()
    diagnosis = Diagnosis(ktools)

    last_info = {"stdout": "", "start_time": int(time.time()), "diagnosis": diagnosis}

    while process_check(ktools, last_info):
        time.sleep(10)
