package dynamicscripts

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.ComponentInstanceStatusEnum
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.common.util.ConditionUtil
import com.alibaba.tesla.appmanager.domain.req.rtcomponentinstance.RtComponentInstanceGetStatusReq
import com.alibaba.tesla.appmanager.domain.res.rtcomponentinstance.RtComponentInstanceGetStatusRes
import com.alibaba.tesla.appmanager.server.dynamicscript.handler.ComponentCustomStatusHandler
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 自定义状态获取脚本: 通用资源 V2
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class CustomStatusGenericResourceV2 implements ComponentCustomStatusHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomStatusGenericResourceV2.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.CUSTOM_STATUS.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "generic-resource/v2"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 8

    /**
     * OpenKruise DaemonSet
     */
    private static final CustomResourceDefinitionContext OPENKRUISE_DAEMONSET = new CustomResourceDefinitionContext.Builder()
            .withName("daemonsets.apps.kruise.io")
            .withGroup("apps.kruise.io")
            .withVersion("v1alpha1")
            .withPlural("daemonsets")
            .withScope("Namespaced")
            .build()

    /**
     * OpenKruise StatefulSet
     */
    private static final CustomResourceDefinitionContext OPENKRUISE_STATEFULSET = new CustomResourceDefinitionContext.Builder()
            .withName("statefulsets.apps.kruise.io")
            .withGroup("apps.kruise.io")
            .withVersion("v1alpha1")
            .withPlural("statefulsets")
            .withScope("Namespaced")
            .build()

    @Override
    RtComponentInstanceGetStatusRes getStatus(
            RtComponentInstanceGetStatusReq request, DefaultKubernetesClient client, JSONObject options) {
        def logSuffix = String.format("clusterId=%s|namespaceId=%s|stageId=%s|appId=%s|componentType=%s|" +
                "componentName=%s|version=%s", request.getClusterId(), request.getNamespaceId(), request.getStageId(),
                request.getAppId(), request.getComponentType(), request.getComponentName(), request.getVersion())
        def endStatus = options.getString("endStatus")
        if (StringUtils.isEmpty(endStatus)) {
            endStatus = ComponentInstanceStatusEnum.COMPLETED.toString()
        }
        def resources = options.getJSONArray("resources")
        if (resources == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "cannot find resources in options")
        }
        def checkResult = new JSONObject()
        for (def resource : resources.toJavaList(JSONObject.class)) {
            def kind = resource.getString("kind")
            def namespace = resource.getString("namespace")
            def resourceOptions = resource.getJSONObject("resourceOptions")
            if (StringUtils.isAnyEmpty(namespace, kind)) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("invalid kind/namespace configuration in options|resource=%s",
                                JSONObject.toJSONString(resource)))
            }

            // 获取 includes / excludes 配置
            def includesJson = resource.getJSONArray("includes")
            def excludesJson = resource.getJSONArray("excludes")
            if (includesJson == null) {
                includesJson = new JSONArray()
            }
            if (excludesJson == null) {
                excludesJson = new JSONArray()
            }
            def includes = includesJson.toJavaList(String.class)
            def excludes = excludesJson.toJavaList(String.class)

            // 根据不同类型的 kind 进行判定状态判定操作
            switch (kind) {
                case "statefulsets":
                    def res = checkStatusfulSet(request, client, options, namespace, includes, excludes, checkResult)
                    if (res != null) {
                        return res
                    }
                    break
                case "deployments":
                    def res = checkDeployment(request, client, options, namespace, includes, excludes, checkResult)
                    if (res != null) {
                        return res
                    }
                    break
                case "daemonsets":
                    def res = checkDaemonSet(request, client, options, namespace,
                            includes, excludes, checkResult, resourceOptions)
                    if (res != null) {
                        return res
                    }
                    break
                case "daemonsets.apps.kruise.io":
                    def res = checkOpenKruiseDaemonSet(request, client, options, namespace,
                            includes, excludes, checkResult, resourceOptions)
                    if (res != null) {
                        return res
                    }
                    break
                case "statefulsets.apps.kruise.io":
                    def res = checkOpenKruiseStatusfulSet(request, client, options, namespace, includes, excludes, checkResult)
                    if (res != null) {
                        return res
                    }
                    break
                default:
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS, "invalid 'kind' value in resource")
            }
        }
        log.info("script=CustomStatusGenericResourceV2|message=all resources compare finished|{}", logSuffix)
        return RtComponentInstanceGetStatusRes.builder()
                .status(endStatus)
                .conditions(ConditionUtil.dataOutputCondition(checkResult))
                .build()
    }

    /**
     * 检测 StatefulSet 的状态
     * @return 如果为 null 说明检测通过；否则说明检测不通过，需要直接返回该状态
     */
    private RtComponentInstanceGetStatusRes checkStatusfulSet(
            RtComponentInstanceGetStatusReq request, DefaultKubernetesClient client, JSONObject options,
            String namespace, List<String> includes, List<String> excludes, JSONObject checkResult) {
        def logSuffix = String.format("clusterId=%s|namespaceId=%s|stageId=%s|appId=%s|componentType=%s|" +
                "componentName=%s|version=%s", request.getClusterId(), request.getNamespaceId(), request.getStageId(),
                request.getAppId(), request.getComponentType(), request.getComponentName(), request.getVersion())
        def includeUsedSet = new HashSet()
        def statefulsets
        try {
            statefulsets = client.apps().statefulSets().inNamespace(namespace).list()
        } catch (Exception e) {
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition(
                            "CheckStatefulSetStatus", "False",
                            String.format("list sts in namespace failed|namespace=%s|%s|exception=%s",
                                    namespace, logSuffix, ExceptionUtils.getStackTrace(e)),
                            ""))
                    .build()
        }
        for (def sts : statefulsets.getItems()) {
            def stsName = sts.getMetadata().getName()
            if (excludes.contains(stsName)) {
                log.info("the sts {} is in the exclusion configuration, skip|{}", stsName, logSuffix)
                continue
            }
            if (includes.size() > 0 && !includes.contains(stsName)) {
                log.info("the sts {} is not in the include configuration, skip|{}", stsName, logSuffix)
                continue
            }
            includeUsedSet.add(stsName)

            // 如果只有 0 个 replicas，那么默认放行
            if (sts.getStatus().getReplicas() == 0) {
                log.info("the replicas of sts {} is 0, skip|{}", stsName, logSuffix)
                continue
            }

            // 判定当前是否进入终态
            def currentRevision = sts.getStatus().getCurrentRevision()
            def updateRevision = sts.getStatus().getUpdateRevision()
            def replicas = sts.getStatus().getReplicas()
            def readyReplicas = sts.getStatus().getReadyReplicas()
            def targetReplicas = sts.getSpec().getReplicas()
            def statusStr = JSONObject.toJSONString(sts.getStatus())
            if (currentRevision == updateRevision && readyReplicas == replicas && targetReplicas == replicas) {
                log.info("the sts {} is in final state, skip|status={}|{}",
                        stsName, statusStr, logSuffix)
                checkResult.putIfAbsent(namespace, new JSONObject())
                checkResult.getJSONObject(namespace).putIfAbsent(stsName, new JSONObject())
                checkResult.getJSONObject(namespace).getJSONObject(stsName).put("status", JSONObject.parseObject(statusStr))
                continue
            }

            // 返回未到终态信息
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition("CheckStatefulSetStatus", "False",
                            String.format("sts not in final state|namespace=%s|" +
                                    "name=%s|status=%s|%s", namespace, stsName, statusStr, logSuffix), ""))
                    .build()
        }

        // 如果 includes 包含内容，且没有全部产出 sts，那么仍然认为不到终态
        for (def includeItem : includes) {
            if (!includeUsedSet.contains(includeItem)) {
                return RtComponentInstanceGetStatusRes.builder()
                        .status(ComponentInstanceStatusEnum.WARNING.toString())
                        .conditions(ConditionUtil.singleCondition("CheckStatefulSetExists", "False",
                                String.format("sts not exists|namespace=%s|" +
                                        "name=%s|%s", namespace, includeItem, logSuffix), ""))
                        .build()
            }
        }
        return null
    }

    /**
     * 检测 OpenKruise StatefulSet 的状态
     * @return 如果为 null 说明检测通过；否则说明检测不通过，需要直接返回该状态
     */
    private RtComponentInstanceGetStatusRes checkOpenKruiseStatusfulSet(
            RtComponentInstanceGetStatusReq request, DefaultKubernetesClient client, JSONObject options,
            String namespace, List<String> includes, List<String> excludes, JSONObject checkResult) {
        def logSuffix = String.format("clusterId=%s|namespaceId=%s|stageId=%s|appId=%s|componentType=%s|" +
                "componentName=%s|version=%s", request.getClusterId(), request.getNamespaceId(), request.getStageId(),
                request.getAppId(), request.getComponentType(), request.getComponentName(), request.getVersion())

        def includeUsedSet = new HashSet()
        def statefulsets
        try {
            statefulsets = client.customResource(OPENKRUISE_STATEFULSET).inNamespace(namespace).list()
        } catch (Exception e) {
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition(
                            "CheckOpenKruiseStatefulSetStatus", "False",
                            String.format("list sts in namespace failed|namespace=%s|%s|exception=%s",
                                    namespace, logSuffix, ExceptionUtils.getStackTrace(e)),
                            ""))
                    .build()
        }

        // 对每一个 daemonsets 进行检测
        def items = statefulsets.get("items") as ArrayList
        for (def item : items) {
            def sts = JSONObject.parseObject(JSONObject.toJSONString(item))
            def metadata = sts.getJSONObject("metadata")
            def stsName = metadata.getString("name")
            if (excludes.contains(stsName)) {
                log.info("the sts {} is in the exclusion configuration, skip|{}", stsName, logSuffix)
                continue
            }
            if (includes.size() > 0 && !includes.contains(stsName)) {
                log.info("the sts {} is not in the include configuration, skip|{}", stsName, logSuffix)
                continue
            }
            includeUsedSet.add(stsName)

            // 如果只有 0 个 replicas，那么默认放行
            if (sts.getJSONObject("status").getInteger("replicas") == 0) {
                log.info("the replicas of sts {} is 0, skip|{}", stsName, logSuffix)
                continue
            }

            // 判定当前是否进入终态
            def currentRevision = sts.getJSONObject("status").getString("currentRevision")
            def updateRevision = sts.getJSONObject("status").getString("updateRevision")
            def replicas = sts.getJSONObject("status").getInteger("replicas")
            def readyReplicas = sts.getJSONObject("status").getInteger("readyReplicas")
            def targetReplicas = sts.getJSONObject("spec").getInteger("replicas")
            def statusStr = JSONObject.toJSONString(sts.getJSONObject("status"))
            if (currentRevision == updateRevision && readyReplicas == replicas && targetReplicas == replicas) {
                log.info("the sts {} is in final state, skip|status={}|{}",
                        stsName, statusStr, logSuffix)
                checkResult.putIfAbsent(namespace, new JSONObject())
                checkResult.getJSONObject(namespace).putIfAbsent(stsName, new JSONObject())
                checkResult.getJSONObject(namespace).getJSONObject(stsName).put("status", JSONObject.parseObject(statusStr))
                continue
            }

            // 返回未到终态信息
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition("CheckOpenKruiseStatefulSetStatus", "False",
                            String.format("sts not in final state|namespace=%s|" +
                                    "name=%s|status=%s|%s", namespace, stsName, statusStr, logSuffix), ""))
                    .build()
        }

        // 如果 includes 包含内容，且没有全部产出 sts，那么仍然认为不到终态
        for (def includeItem : includes) {
            if (!includeUsedSet.contains(includeItem)) {
                return RtComponentInstanceGetStatusRes.builder()
                        .status(ComponentInstanceStatusEnum.WARNING.toString())
                        .conditions(ConditionUtil.singleCondition("CheckOpenKruiseStatefulSetExists", "False",
                                String.format("sts not exists|namespace=%s|" +
                                        "name=%s|%s", namespace, includeItem, logSuffix), ""))
                        .build()
            }
        }
        return null
    }

    /**
     * 检测 Deployment 的状态
     * @return 如果为 null 说明检测通过；否则说明检测不通过，需要直接返回该状态
     */
    private RtComponentInstanceGetStatusRes checkDeployment(
            RtComponentInstanceGetStatusReq request, DefaultKubernetesClient client, JSONObject options,
            String namespace, List<String> includes, List<String> excludes, JSONObject checkResult) {
        def logSuffix = String.format("clusterId=%s|namespaceId=%s|stageId=%s|appId=%s|componentType=%s|" +
                "componentName=%s|version=%s", request.getClusterId(), request.getNamespaceId(), request.getStageId(),
                request.getAppId(), request.getComponentType(), request.getComponentName(), request.getVersion())
        def includeUsedSet = new HashSet()
        def deployments
        try {
            deployments = client.apps().deployments().inNamespace(namespace).list()
        } catch (Exception e) {
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition(
                            "CheckDeploymentStatus", "False",
                            String.format("list deployment in namespace failed|namespace=%s|%s|exception=%s",
                                    namespace, logSuffix, ExceptionUtils.getStackTrace(e)),
                            ""))
                    .build()
        }
        for (def deploy : deployments.getItems()) {
            def deployName = deploy.getMetadata().getName()
            if (excludes.contains(deployName)) {
                log.info("the deployment {} is in the exclusion configuration, skip|{}", deployName, logSuffix)
                continue
            }
            if (includes.size() > 0 && !includes.contains(deployName)) {
                log.info("the deployment {} is not in the include configuration, skip|{}", deployName, logSuffix)
                continue
            }
            includeUsedSet.add(deployName)

            // 如果只有 0 个 replicas，那么默认放行
            if (deploy.getStatus().getReplicas() == 0) {
                log.info("the replicas of deployment {} is 0, skip|{}", deployName, logSuffix)
                continue
            }

            // 判定当前是否进入终态
            def replicas = deploy.getStatus().getReplicas()
            def readyReplicas = deploy.getStatus().getReadyReplicas()
            def updateReplicas = deploy.getStatus().getUpdatedReplicas()
            def statusStr = JSONObject.toJSONString(deploy.getStatus())
            if (readyReplicas == replicas && updateReplicas == replicas) {
                log.info("the deployment {} is in final state, skipd|status={}|{}",
                        deployName, statusStr, logSuffix)
                checkResult.putIfAbsent(namespace, new JSONObject())
                checkResult.getJSONObject(namespace).putIfAbsent(deployName, new JSONObject())
                checkResult.getJSONObject(namespace).getJSONObject(deployName)
                        .put("status", JSONObject.parseObject(statusStr))
                continue
            }

            // 返回未到终态信息
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition("CheckDeploymentStatus", "False",
                            String.format("deployment not in final state|namespace=%s|" +
                                    "name=%s|status=%s|%s", namespace, deployName, statusStr, logSuffix), ""))
                    .build()
        }

        // 如果 includes 包含内容，且没有全部产出 deployment，那么仍然认为不到终态
        for (def includeItem : includes) {
            if (!includeUsedSet.contains(includeItem)) {
                return RtComponentInstanceGetStatusRes.builder()
                        .status(ComponentInstanceStatusEnum.WARNING.toString())
                        .conditions(ConditionUtil.singleCondition("CheckDeploymentExists", "False",
                                String.format("deployment not exists|namespace=%s|" +
                                        "name=%s|%s", namespace, includeItem, logSuffix), ""))
                        .build()
            }
        }
        return null
    }

    /**
     * 检测 DaemonSet 的状态
     * @return 如果为 null 说明检测通过；否则说明检测不通过，需要直接返回该状态
     */
    private RtComponentInstanceGetStatusRes checkDaemonSet(
            RtComponentInstanceGetStatusReq request, DefaultKubernetesClient client, JSONObject options,
            String namespace, List<String> includes, List<String> excludes, JSONObject checkResult,
            JSONObject resourceOptions) {
        def logSuffix = String.format("clusterId=%s|namespaceId=%s|stageId=%s|appId=%s|componentType=%s|" +
                "componentName=%s|version=%s", request.getClusterId(), request.getNamespaceId(), request.getStageId(),
                request.getAppId(), request.getComponentType(), request.getComponentName(), request.getVersion())

        // 初始化不可用参数配置 (-1 代表不使用该参数)
        def unavailablePercentage = -1
        def unavailableNumber = -1
        if (resourceOptions == null) {
            unavailableNumber = 0
        } else {
            if (resourceOptions.getInteger("unavailablePercentage") != null) {
                unavailablePercentage = resourceOptions.getInteger("unavailablePercentage")
                if (unavailablePercentage < 0 || unavailablePercentage > 100) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            "invalid unavailablePercentage in daemonsets watch config")
                }
            }
            if (resourceOptions.getInteger("unavailableNumber") != null) {
                unavailableNumber = resourceOptions.getInteger("unavailableNumber")
                if (unavailableNumber < 0) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            "invalid unavailableNumber in daemonsets watch config")
                }
            }
        }

        // 获取集群中的 daemonsets
        def includeUsedSet = new HashSet()
        def daemonsets
        try {
            daemonsets = client.apps().daemonSets().inNamespace(namespace).list()
        } catch (Exception e) {
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition(
                            "CheckDaemonSetStatus", "False",
                            String.format("list daemonset in namespace failed|namespace=%s|%s|exception=%s",
                                    namespace, logSuffix, ExceptionUtils.getStackTrace(e)),
                            ""))
                    .build()
        }

        // 对每一个 daemonsets 进行检测
        for (def daemonset : daemonsets.getItems()) {
            def daemonsetName = daemonset.getMetadata().getName()
            if (excludes.contains(daemonsetName)) {
                log.info("the daemonset {} is in the exclusion configuration, skip|{}", daemonsetName, logSuffix)
                continue
            }
            if (includes.size() > 0 && !includes.contains(daemonsetName)) {
                log.info("the daemonset {} is not in the include configuration, skip|{}", daemonsetName, logSuffix)
                continue
            }
            includeUsedSet.add(daemonsetName)

            // 判定当前是否进入终态
            def statusStr = JSONObject.toJSONString(daemonset.getStatus())
            int desiredNumberScheduled = daemonset.getStatus().getDesiredNumberScheduled()
            int currentNumberScheduled = daemonset.getStatus().getCurrentNumberScheduled()
            def numberReady = daemonset.getStatus().getNumberReady()
            if (desiredNumberScheduled == currentNumberScheduled && currentNumberScheduled == numberReady) {
                log.info("the daemonset {} is in final state, skip|status={}|{}",
                        daemonsetName, statusStr, logSuffix)
                checkResult.putIfAbsent(namespace, new JSONObject())
                checkResult.getJSONObject(namespace).putIfAbsent(daemonsetName, new JSONObject())
                checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                        .put("status", JSONObject.parseObject(statusStr))
                continue
            }

            // 如果满足最小可用数量，也可以认为终态
            if (unavailableNumber != -1 && desiredNumberScheduled - numberReady <= unavailableNumber) {
                log.info("the daemonset {} is in final state (unavailableNumber), skip|status={}|{}",
                        daemonsetName, statusStr, logSuffix)
                checkResult.putIfAbsent(namespace, new JSONObject())
                checkResult.getJSONObject(namespace).putIfAbsent(daemonsetName, new JSONObject())
                checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                        .put("status", JSONObject.parseObject(statusStr))
                checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                        .put("userSetUnavailableNumber", unavailableNumber)
                continue
            }

            // 如果满足最小可用百分比，也可以认为终态
            if (unavailablePercentage != -1) {
                def percentage = (double) (desiredNumberScheduled - numberReady) / desiredNumberScheduled * 100
                if (percentage <= (double) unavailablePercentage) {
                    log.info("the daemonset {} is in final state (unavailablePercentage), skip|status={}|{}",
                            daemonsetName, statusStr, logSuffix)
                    checkResult.putIfAbsent(namespace, new JSONObject())
                    checkResult.getJSONObject(namespace).putIfAbsent(daemonsetName, new JSONObject())
                    checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                            .put("status", JSONObject.parseObject(statusStr))
                    checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                            .put("userSetUnavailablePercentage", unavailablePercentage)
                    continue
                }
            }

            // 返回未到终态信息
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition("CheckDaemonSetStatus", "False",
                            String.format("daemonset not in final state|namespace=%s|" +
                                    "name=%s|status=%s|%s", namespace, daemonsetName, statusStr, logSuffix), ""))
                    .build()
        }

        // 如果 includes 包含内容，且没有全部产出 daemonset，那么仍然认为不到终态
        for (def includeItem : includes) {
            if (!includeUsedSet.contains(includeItem)) {
                return RtComponentInstanceGetStatusRes.builder()
                        .status(ComponentInstanceStatusEnum.WARNING.toString())
                        .conditions(ConditionUtil.singleCondition("CheckDaemonSetExists", "False",
                                String.format("daemonset not exists|namespace=%s|" +
                                        "name=%s|%s", namespace, includeItem, logSuffix), ""))
                        .build()
            }
        }
        return null
    }

    /**
     * 检测 DaemonSet 的状态
     * @return 如果为 null 说明检测通过；否则说明检测不通过，需要直接返回该状态
     */
    private RtComponentInstanceGetStatusRes checkOpenKruiseDaemonSet(
            RtComponentInstanceGetStatusReq request, DefaultKubernetesClient client, JSONObject options,
            String namespace, List<String> includes, List<String> excludes, JSONObject checkResult,
            JSONObject resourceOptions) {
        def logSuffix = String.format("clusterId=%s|namespaceId=%s|stageId=%s|appId=%s|componentType=%s|" +
                "componentName=%s|version=%s", request.getClusterId(), request.getNamespaceId(), request.getStageId(),
                request.getAppId(), request.getComponentType(), request.getComponentName(), request.getVersion())

        // 初始化不可用参数配置 (-1 代表不使用该参数)
        def unavailablePercentage = -1
        def unavailableNumber = -1
        if (resourceOptions == null) {
            unavailableNumber = 0
        } else {
            if (resourceOptions.getInteger("unavailablePercentage") != null) {
                unavailablePercentage = resourceOptions.getInteger("unavailablePercentage")
                if (unavailablePercentage < 0 || unavailablePercentage > 100) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            "invalid unavailablePercentage in daemonsets watch config")
                }
            }
            if (resourceOptions.getInteger("unavailableNumber") != null) {
                unavailableNumber = resourceOptions.getInteger("unavailableNumber")
                if (unavailableNumber < 0) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            "invalid unavailableNumber in daemonsets watch config")
                }
            }
        }

        // 获取集群中的 daemonsets
        def includeUsedSet = new HashSet()
        def daemonsets
        try {
            daemonsets = client.customResource(OPENKRUISE_DAEMONSET).inNamespace(namespace).list()
        } catch (Exception e) {
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition(
                            "CheckDaemonSetStatus", "False",
                            String.format("list daemonset(kruise) in namespace failed|namespace=%s|%s|exception=%s",
                                    namespace, logSuffix, ExceptionUtils.getStackTrace(e)),
                            ""))
                    .build()
        }

        // 对每一个 daemonsets 进行检测
        def items = daemonsets.get("items") as ArrayList
        for (def item : items) {
            def daemonset = JSONObject.parseObject(JSONObject.toJSONString(item))
            def metadata = daemonset.getJSONObject("metadata")
            if (metadata == null || StringUtils.isEmpty(metadata.getString("name"))) {
                return RtComponentInstanceGetStatusRes.builder()
                        .status(ComponentInstanceStatusEnum.WARNING.toString())
                        .conditions(ConditionUtil.singleCondition(
                                "CheckDaemonSetMetadata", "False",
                                String.format("get daemonset(kruise) metadata in namespace failed|" +
                                        "namespace=%s|%s|item=%s", namespace, logSuffix, daemonset.toJSONString()),
                                ""))
                        .build()
            }
            def daemonsetName = metadata.getString("name")
            if (excludes.contains(daemonsetName)) {
                log.info("the daemonset {} is in the exclusion configuration, skip|{}", daemonsetName, logSuffix)
                continue
            }
            if (includes.size() > 0 && !includes.contains(daemonsetName)) {
                log.info("the daemonset {} is not in the include configuration, skip|{}", daemonsetName, logSuffix)
                continue
            }
            includeUsedSet.add(daemonsetName)

            // 判定当前是否进入终态
            def statusStr = JSONObject.toJSONString(daemonset.getJSONObject("status"))
            int desiredNumberScheduled = daemonset.getJSONObject("status").getInteger("desiredNumberScheduled")
            int currentNumberScheduled = daemonset.getJSONObject("status").getInteger("currentNumberScheduled")
            def numberReady = daemonset.getJSONObject("status").getInteger("numberReady")
            if (desiredNumberScheduled == currentNumberScheduled && currentNumberScheduled == numberReady) {
                log.info("the daemonset {} is in final state, skip|status={}|{}",
                        daemonsetName, statusStr, logSuffix)
                checkResult.putIfAbsent(namespace, new JSONObject())
                checkResult.getJSONObject(namespace).putIfAbsent(daemonsetName, new JSONObject())
                checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                        .put("status", JSONObject.parseObject(statusStr))
                continue
            }

            // 如果满足最小可用数量，也可以认为终态
            if (unavailableNumber != -1 && desiredNumberScheduled - numberReady <= unavailableNumber) {
                log.info("the daemonset {} is in final state (unavailableNumber), skip|status={}|{}",
                        daemonsetName, statusStr, logSuffix)
                checkResult.putIfAbsent(namespace, new JSONObject())
                checkResult.getJSONObject(namespace).putIfAbsent(daemonsetName, new JSONObject())
                checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                        .put("status", JSONObject.parseObject(statusStr))
                checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                        .put("userSetUnavailableNumber", unavailableNumber)
                continue
            }

            // 如果满足最小可用百分比，也可以认为终态
            if (unavailablePercentage != -1) {
                def percentage = (double) (desiredNumberScheduled - numberReady) / desiredNumberScheduled * 100
                if (percentage <= (double) unavailablePercentage) {
                    log.info("the daemonset {} is in final state (unavailablePercentage), skip|status={}|{}",
                            daemonsetName, statusStr, logSuffix)
                    checkResult.putIfAbsent(namespace, new JSONObject())
                    checkResult.getJSONObject(namespace).putIfAbsent(daemonsetName, new JSONObject())
                    checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                            .put("status", JSONObject.parseObject(statusStr))
                    checkResult.getJSONObject(namespace).getJSONObject(daemonsetName)
                            .put("userSetUnavailablePercentage", unavailablePercentage)
                    continue
                }
            }

            // 返回未到终态信息
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                    .conditions(ConditionUtil.singleCondition("CheckDaemonSetStatus", "False",
                            String.format("daemonset not in final state|namespace=%s|" +
                                    "name=%s|status=%s|%s", namespace, daemonsetName, statusStr, logSuffix), ""))
                    .build()
        }

        // 如果 includes 包含内容，且没有全部产出 daemonset，那么仍然认为不到终态
        for (def includeItem : includes) {
            if (!includeUsedSet.contains(includeItem)) {
                return RtComponentInstanceGetStatusRes.builder()
                        .status(ComponentInstanceStatusEnum.WARNING.toString())
                        .conditions(ConditionUtil.singleCondition("CheckDaemonSetExists", "False",
                                String.format("daemonset not exists|namespace=%s|" +
                                        "name=%s|%s", namespace, includeItem, logSuffix), ""))
                        .build()
            }
        }
        return null
    }
}