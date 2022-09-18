package dynamicscripts

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.domain.core.WorkloadResource
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import com.google.common.collect.ImmutableMap
import io.fabric8.kubernetes.api.model.Secret
import io.fabric8.kubernetes.api.model.SecretBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.dsl.Resource
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import java.nio.charset.StandardCharsets

/**
 * Secret Trait
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class TraitSecret implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitSecret.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "secret.trait.abm.io"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 1

    @Autowired
    private KubernetesClientFactory clientFactory

    /**
     * Trait 业务侧逻辑执行
     *
     * @param request Trait 输入参数
     * @return Trait 修改后的 Spec 定义
     */
    @Override
    TraitExecuteRes execute(TraitExecuteReq request) {
        /**
         * 1. get metadata from workload
         */
        log.info("start execute secret trait {}", request.getSpec().toJSONString())

        WorkloadResource workloadResource = request.getRef()
        String namespace = workloadResource.getMetadata().getNamespace()
        JSONObject labels = request.getSpec().getJSONObject("labels")
        JSONObject annotations = request.getSpec().getJSONObject("annotations")
        request.getSpec().remove("labels")
        request.getSpec().remove("annotations")
        labels = labels == null ? new JSONObject() : labels
        annotations = annotations == null ? new JSONObject() : annotations

        /**
         * 2. get k8s cluster info
         */
        WorkloadResource workloadRef = request.getRef()
        String clusterId = request.getComponent().getClusterId()
        if (StringUtils.isEmpty(clusterId)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find clusterId in workload labels|workload=%s",
                            JSONObject.toJSONString(workloadRef)))
        }
        DefaultKubernetesClient client = clientFactory.get(clusterId)

        /**
         * 3. get secrets and apply to k8s cluster
         */
        JSONObject secrets = request.getSpec().getJSONObject("secrets")
        log.info("start to apply secret in secret trait {}", secrets.toJSONString())
        for (Map.Entry<String, Object> item : secrets.entrySet()) {
            String secretName = item.getKey()
            Object secretData = item.getValue()
            JSONObject secret = generateSecret(namespace, secretName, secretData, labels, annotations)
            applySecret(request, client, secret, labels, annotations)
        }

        /**
         4. patch targets in values
         */
        JSONArray targets = request.getSpec().getJSONArray("targets")
        if (targets == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "")
        }
        for (int i = 0; i < targets.size(); i++) {
            JSONObject target = targets.getJSONObject(i)
            if ("env" == target.getString("type")) {
                /**
                 *   containers:
                 *   - name: test-container
                 *     image: k8s.gcr.io/busybox
                 *     command: [ "/bin/sh", "-c", "env" ]
                 *     envFrom:
                 *     - secretRef:
                 *         name: special-config
                 */
                if (target.getString("secret") == null) {
                    String errorMessage = String.format("secret key not found %s", target.toJSONString())
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage)
                }
                JSONArray updateDatas = new JSONArray()
                updateDatas.add(JSONObject.parseObject(JSONObject.toJSONString(ImmutableMap.of(
                        "secretRef", ImmutableMap.of(
                        "name", target.getString("secret")) as Object
                )
                )))
                if (target.getString("initContainer") != null) {
                    updateContainers(request, "initContainer", target.getString("initContainer"), "envFrom", updateDatas)
                } else if (target.getString("container") != null) {
                    updateContainers(request, "container", target.getString("container"), "envFrom", updateDatas)
                } else {
                    String errorMessage = String.format("not found container or initContainer %s", target.toJSONString())
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage)
                }
            } else if ("volumeMount" == target.getString("type")) {
                /**
                 *  containers:
                 *  - name: test-container
                 *    image: k8s.gcr.io/busybox
                 *    command: [ "/bin/sh", "-c", "ls /etc/config/" ]
                 *    volumeMounts:
                 *    - name: config-volume
                 *      mountPath: /etc/config
                 *  volumes:
                 *     - name: config-volume
                 *       secret:
                 *         name: special-config
                 */
                if (target.getString("secret") == null || target.getString("mountPath") == null) {
                    String errorMessage = String.format("secret or mountPath not found %s", target.toJSONString())
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage)
                }
                JSONArray volumes = new JSONArray()
                volumes.add(JSONObject.parseObject(JSONObject.toJSONString(ImmutableMap.of(
                        "name", target.getString("secret"),
                        "secret", ImmutableMap.of(
                        "secretName", target.getString("secret")
                )
                ))))
                updateVolumes(request, volumes)
                JSONArray updateDatas = new JSONArray()
                if (StringUtils.isNotEmpty(target.getString("subPath"))) {
                    updateDatas.add(JSONObject.parseObject(JSONObject.toJSONString(ImmutableMap.of(
                            "name", target.getString("secret"),
                            "mountPath", target.getString("mountPath"),
                            "subPath", target.getString("subPath")
                    ))))
                } else {
                    updateDatas.add(JSONObject.parseObject(JSONObject.toJSONString(ImmutableMap.of(
                            "name", target.getString("secret"),
                            "mountPath", target.getString("mountPath")
                    ))))
                }
                if (target.getString("initContainer") != null) {
                    updateContainers(request, "initContainer", target.getString("initContainer"), "volumeMounts", updateDatas)
                } else if (target.getString("container") != null) {
                    updateContainers(request, "container", target.getString("container"), "volumeMounts", updateDatas)
                } else {
                    String errorMessage = String.format("not found container or initContainer %s", target.toJSONString())
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage)
                }
            } else if ("envValue" == target.getString("type")) {
                /**
                 *   containers:
                 *     - name: test-container
                 *       image: k8s.gcr.io/busybox
                 *       command: [ "/bin/sh", "-c", "echo $(SPECIAL_LEVEL_KEY) $(SPECIAL_TYPE_KEY)" ]
                 *       env:
                 *         - name: SPECIAL_LEVEL_KEY
                 *           valueFrom:
                 *             secretKeyRef:
                 *               name: special-config
                 *               key: SPECIAL_LEVEL
                 */
                JSONArray updateEnvs = new JSONArray()
                for (int j = 0; j < target.getJSONArray("values").size(); j++) {
                    JSONObject v = target.getJSONArray("values").getJSONObject(j)
                    updateEnvs.add(JSONObject.parseObject(JSONObject.toJSONString(ImmutableMap.of(
                            "name", v.getString("name"),
                            "valueFrom", ImmutableMap.of(
                            "secretKeyRef", ImmutableMap.of(
                            "name", v.getString("secret"),
                            "key", v.getString("key")
                    ) as Object
                    )
                    ))))
                }
                if (target.getString("initContainer") != null) {
                    updateContainers(request, "initContainer", target.getString("initContainer"), "env", updateEnvs)
                } else if (target.getString("container") != null) {
                    updateContainers(request, "container", target.getString("container"), "env", updateEnvs)
                } else {
                    String errorMessage = String.format("not found container or initContainer %s", target.toJSONString())
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage)
                }
            }
        }
        return TraitExecuteRes.builder()
                .spec(request.getSpec())
                .build()
    }

    /**
     * update containers in string
     *
     * @param type container or initContainer
     * @param name
     * @param targetKey
     * @param updateDatas
     * @return
     */
    private static void updateContainers(TraitExecuteReq request, String type, String name, String targetKey, JSONArray updateDatas) {
        JSONObject workloadSpec = (JSONObject) request.getRef().getSpec()
        log.info("secret trait parent workload {}", workloadSpec.toJSONString())
        JSONArray containers
        if (workloadSpec.get("cloneSet") != null) {
            JSONObject cloneSetSpec = workloadSpec
                    .getJSONObject("cloneSet")
                    .getJSONObject("template")
                    .getJSONObject("spec")
            containers = cloneSetSpec.getJSONArray(type + "s")
        } else if (workloadSpec.get("advancedStatefulSet") != null) {
            JSONObject advancedStatefulSetSpec = workloadSpec
                    .getJSONObject("advancedStatefulSet")
                    .getJSONObject("template")
                    .getJSONObject("spec")
            containers = advancedStatefulSetSpec.getJSONArray(type + "s")
        } else if ("Deployment" == workloadSpec.getString("kind")) {
            containers = workloadSpec.getJSONArray(type + "s")
        } else {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "not supported")
        }
        for (int i = 0; i < containers.size(); i++) {
            JSONObject container = containers.getJSONObject(i)
            if (!Objects.equals(container.getString("name"), name)) {
                log.info("container name not match {} {}", container.getString("name"), name)
                continue
            }
            container.putIfAbsent(targetKey, new JSONArray())
            JSONArray target = container.getJSONArray(targetKey)
            for (int j = 0; j < updateDatas.size(); j++) {
                JSONObject updateData = updateDatas.getJSONObject(i)
                log.info("update container {} {}", target.toJSONString(), updateData.toJSONString())
                target.add(updateData)
            }
        }
        log.info("secret trait parent workload after update {}", workloadSpec.toJSONString())
    }

    /**
     * update volumes in string
     *
     * @param updateDatas
     * @return
     */
    private static void updateVolumes(TraitExecuteReq request, JSONArray updateDatas) {
        JSONObject workloadSpec = (JSONObject) request.getRef().getSpec()
        JSONArray volumes
        if (workloadSpec.get("cloneSet") != null) {
            JSONObject cloneSetSpec = workloadSpec
                    .getJSONObject("cloneSet")
                    .getJSONObject("template")
                    .getJSONObject("spec")
            volumes = cloneSetSpec.getJSONArray("volumes")
            if (volumes == null) {
                cloneSetSpec.put("volumes", new JSONArray())
                volumes = cloneSetSpec.getJSONArray("volumes")
            }
        } else if (workloadSpec.get("advancedStatefulSet") != null) {
            JSONObject advancedStatefulSetSpec = workloadSpec
                    .getJSONObject("advancedStatefulSet")
                    .getJSONObject("template")
                    .getJSONObject("spec")
            volumes = advancedStatefulSetSpec.getJSONArray("volumes")
            if (volumes == null) {
                advancedStatefulSetSpec.put("volumes", new JSONArray())
                volumes = advancedStatefulSetSpec.getJSONArray("volumes")
            }
        } else {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "not supported")
        }
        for (int i = 0; i < updateDatas.size(); i++) {
            JSONObject updateData = updateDatas.getJSONObject(i)
            volumes.add(updateData)
        }
    }

    /**
     * 创建配置文件 Secret
     *
     * @param namespace 命名空间
     * @param name 标识名称
     * @param data 配置数据
     * @return JSONObject
     */
    private static JSONObject generateSecret(
            String namespace, String name, Object data, Object labels, Object annotations) {
        String secretStr = JSONObject.toJSONString(ImmutableMap.of(
                "apiVersion", "v1",
                "kind", "Secret",
                "metadata", ImmutableMap.of(
                "namespace", namespace,
                "name", name,
                "labels", labels,
                "annotations", annotations
        ),
                "data", data
        ))
        JSONObject secret = JSONObject.parseObject(secretStr)
        return secret
    }

    /**
     * secret apply k8s cluster
     *
     * @param client 已经被实例化的k8s连接客户端
     * @param secret 待提交的secret cr对象
     * @param labels
     * @param annotations
     * @return
     */
    private static void applySecret(
            TraitExecuteReq request, DefaultKubernetesClient client, JSONObject secret,
            JSONObject labels, JSONObject annotations) {
        // 应用到集群
        try {
            String namespace = request.getComponent().getNamespaceId()
            String name = secret.getJSONObject("metadata").getString("name")
            Resource<Secret> resource = client.secrets()
                    .load(new ByteArrayInputStream(secret.toJSONString().getBytes(StandardCharsets.UTF_8)))
            try {
                Secret current = client.secrets().inNamespace(namespace).withName(name).get()
                if (current == null) {
                    Secret result = resource.create()
                    log.info("cr yaml has created in kubernetes|cluster={}|namespace={}|name={}|cr={}" +
                            "result={}", client, namespace, name, secret.toJSONString(),
                            JSONObject.toJSONString(result))
                } else {
                    Map<String, String> newData = new HashMap<>()
                    for (Map.Entry<String, Object> entry : secret.getJSONObject("data").entrySet()) {
                        newData.put(entry.getKey(), String.valueOf(entry.getValue()))
                    }
                    JSONObject finalLabels = labels
                    JSONObject finalAnnotations = annotations
                    Secret result = client.secrets()
                            .inNamespace(namespace)
                            .withName(name)
                            .edit(s -> new SecretBuilder(s)
                                    .editMetadata()
                                    .withLabels(JSON.parseObject(finalLabels.toJSONString(), new TypeReference<Map<String, String>>() {
                                    }))
                                    .withAnnotations(JSON.parseObject(finalAnnotations.toJSONString(), new TypeReference<Map<String, String>>() {
                                    }))
                                    .endMetadata()
                                    .withData(newData)
                                    .build())
                    log.info("cr yaml has updated in kubernetes|cluster={}|namespace={}|name={}|labels={}|" +
                            "annotations={}|newData={}|result={}", client, namespace, name,
                            JSONObject.toJSONString(labels), JSONObject.toJSONString(annotations),
                            JSONObject.toJSONString(newData), JSONObject.toJSONString(result))
                }
            } catch (KubernetesClientException e) {
                if (e.getCode() == 422) {
                    log.error("service apply failed, exception={}", ExceptionUtils.getStackTrace(e))
                } else {
                    throw e
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("apply cr yaml to kubernetes failed|cluster=%s|" +
                    "exception=%s|cr=%s", client, ExceptionUtils.getStackTrace(e),
                    secret.toJSONString())
            log.error(errorMessage)
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage)
        }
    }
}
