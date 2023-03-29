package dynamicscripts

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.ComponentInstanceStatusEnum
import com.alibaba.tesla.appmanager.common.enums.DeployComponentAttrTypeEnum
import com.alibaba.tesla.appmanager.common.enums.DeployComponentStateEnum
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.common.util.ConditionUtil
import com.alibaba.tesla.appmanager.common.util.SchemaUtil
import com.alibaba.tesla.appmanager.domain.req.componentinstance.ReportRtComponentInstanceStatusReq
import com.alibaba.tesla.appmanager.domain.req.deploy.GetDeployComponentHandlerReq
import com.alibaba.tesla.appmanager.domain.req.deploy.LaunchDeployComponentHandlerReq
import com.alibaba.tesla.appmanager.domain.res.deploy.GetDeployComponentHandlerRes
import com.alibaba.tesla.appmanager.domain.res.deploy.LaunchDeployComponentHandlerRes
import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory
import com.alibaba.tesla.appmanager.server.repository.condition.RtComponentInstanceQueryCondition
import com.alibaba.tesla.appmanager.server.service.deploy.DeployComponentService
import com.alibaba.tesla.appmanager.server.service.deploy.handler.DeployComponentHandler
import com.alibaba.tesla.appmanager.server.service.rtcomponentinstance.RtComponentInstanceService
import com.google.common.base.Enums
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import java.time.LocalDateTime
import java.time.ZoneId
/**
 * 默认部署 ABM Status Component Groovy Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class ScriptComponentDeployHandler implements DeployComponentHandler {

    private static final Logger log = LoggerFactory.getLogger(ScriptComponentDeployHandler.class)

    /**
     * 当前脚本类型 (ComponentKindEnum)
     */
    public static final String KIND = DynamicScriptKindEnum.COMPONENT_DEPLOY.toString()

    /**
     * 当前脚本名称 (指定 SCRIPT_KIND 下唯一)
     */
    public static final String NAME = "ScriptComponentDefault"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 6

    /**
     * 上报状态常量
     */
    private static final Integer REPORT_MAX_RETRY_TIMES = 10
    private static final Integer REPORT_SLEEP_MILLI = 100

    /**
     * 等待部署超时时间
     */
    private static final long WAIT_TIMEOUT_SECONDS = 1200L

    private static final String ANNOTATIONS_VERSION = "annotations.appmanager.oam.dev/version"
    private static final String ANNOTATIONS_COMPONENT_INSTANCE_ID = "annotations.appmanager.oam.dev/componentInstanceId"
    private static final String ANNOTATIONS_APP_INSTANCE_NAME = "annotations.appmanager.oam.dev/appInstanceName"

    @Autowired
    private KubernetesClientFactory kubernetesClientFactory

    @Autowired
    private RtComponentInstanceService rtComponentInstanceService

    @Autowired
    private DeployComponentService deployComponentService

    /**
     * 部署组件过程
     *
     * @param request 部署请求
     */
    @Override
    LaunchDeployComponentHandlerRes launch(LaunchDeployComponentHandlerReq request) {
        def componentSchema = request.getComponentSchema()
        def annotations = (JSONObject) componentSchema.getSpec().getWorkload().getMetadata().getAnnotations()
        def version = (String) annotations.getOrDefault(ANNOTATIONS_VERSION, "")
        def componentInstanceId = (String) annotations.getOrDefault(ANNOTATIONS_COMPONENT_INSTANCE_ID, "")
        def appInstanceName = (String) annotations.getOrDefault(ANNOTATIONS_APP_INSTANCE_NAME, "")
        def reportSuccess = false
        for (int i = 0; i < REPORT_MAX_RETRY_TIMES; i++) {
            try {
                rtComponentInstanceService.report(ReportRtComponentInstanceStatusReq.builder()
                        .componentInstanceId(componentInstanceId)
                        .appInstanceName(appInstanceName)
                        .clusterId(request.getClusterId())
                        .namespaceId(request.getNamespaceId())
                        .stageId(request.getStageId())
                        .appId(request.getAppId())
                        .componentType(request.getComponentType())
                        .componentName(request.getComponentName())
                        .version(version)
                        .status(ComponentInstanceStatusEnum.UPDATING.toString())
                        .conditions(new ArrayList<>())
                        .build(), false)
                log.info("report abm status component instance to UPDATING success|componentInstanceId={}|" +
                        "appInstanceName={}|clusterId={}|namespaceId={}|stageId={}|appId={}|componentType={}|" +
                        "componentName={}|version={}", componentInstanceId, appInstanceName, request.getClusterId(),
                request.getNamespaceId(), request.getStageId(), request.getAppId(), request.getComponentType(),
                request.getComponentName(), version)
                reportSuccess = true
            } catch (AppException e) {
                if (AppErrorCode.LOCKER_VERSION_EXPIRED == e.getErrorCode()) {
                    Thread.sleep(REPORT_SLEEP_MILLI)
                } else {
                    throw e
                }
            }
        }
        if (!reportSuccess) {
            throw new AppException(AppErrorCode.DEPLOY_ERROR,
                    String.format("cannot update component instance status to UPDATING|request=%s",
                            JSONObject.toJSONString(request)))
        }

        return LaunchDeployComponentHandlerRes.builder()
                .componentSchema(componentSchema)
                .build()
    }

    /**
     * 查询部署组件结果
     *
     * @param request 部署请求
     * @return 查询结果
     */
    @Override
    GetDeployComponentHandlerRes get(GetDeployComponentHandlerReq request) {
        def appId = request.getAppId()
        def clusterId = request.getClusterId()
        def namespaceId = request.getNamespaceId()
        def stageId = request.getStageId()
        def componentType = request.getComponentType()
        def componentName = request.getComponentName()
        def logSuffix = String.format("appId=%s|clusterId=%s|namespaceId=%s|stageId=%s|componentType=%s|" +
                "componentName=%s", appId, clusterId, namespaceId, stageId, componentType, componentName)

        def deployComponent = deployComponentService.get(request.getDeployComponentId(), true)
        def componentInstance = rtComponentInstanceService.get(RtComponentInstanceQueryCondition.builder()
                .appId(appId)
                .clusterId(clusterId)
                .namespaceId(namespaceId)
                .stageId(stageId)
                .componentType(componentType)
                .componentName(componentName)
                .build())
        if (deployComponent == null || componentInstance == null) {
            return GetDeployComponentHandlerRes.builder()
                    .status(DeployComponentStateEnum.WAIT_FOR_OP)
                    .message(String.format("cannot get component when fetching status|%s", logSuffix))
                    .build()
        }

        def componentInstanceStatus = Enums
                .getIfPresent(ComponentInstanceStatusEnum.class, componentInstance.getStatus()).orNull()
        if (componentInstanceStatus == null) {
            return GetDeployComponentHandlerRes.builder()
                    .status(DeployComponentStateEnum.WAIT_FOR_OP)
                    .message(String.format("cannot convert null component instance status|%s", logSuffix))
                    .build()
        }
        def componentSchema = SchemaUtil.toSchema(ComponentSchema.class, deployComponent.getAttrMap()
                .get(DeployComponentAttrTypeEnum.COMPONENT_SCHEMA.toString()))
        long waitTimeoutSeconds = ((JSONObject) componentSchema.getSpec().getWorkload().getSpec())
                .getLongValue("waitTimeoutSeconds")
        if (waitTimeoutSeconds <= 0) {
            waitTimeoutSeconds = WAIT_TIMEOUT_SECONDS
        }

        // 判断是否超时
        def borderDatetime = LocalDateTime.now().minusSeconds(waitTimeoutSeconds)
        def createDatetime = LocalDateTime
                .ofInstant(deployComponent.getSubOrder().getGmtCreate().toInstant(), ZoneId.systemDefault())
        if (createDatetime < borderDatetime) {
            return GetDeployComponentHandlerRes.builder()
                    .status(DeployComponentStateEnum.WAIT_FOR_OP)
                    .message(String.format("wait too long for the component %s to complete, status=%s, conditions=%s",
                            componentName, componentInstance.getStatus(), componentInstance.getConditions()))
                    .build()
        }

        // 根据状态判定当前是否应该结束
        switch (componentInstanceStatus) {
            case ComponentInstanceStatusEnum.COMPLETED:
            case ComponentInstanceStatusEnum.RUNNING:
                break
            case ComponentInstanceStatusEnum.PENDING:
            case ComponentInstanceStatusEnum.UPDATING:
            case ComponentInstanceStatusEnum.WARNING:
            case ComponentInstanceStatusEnum.ERROR:
            case ComponentInstanceStatusEnum.PREPARING_DELETE:
            case ComponentInstanceStatusEnum.PREPARING_UPDATE:
                return GetDeployComponentHandlerRes.builder()
                        .status(DeployComponentStateEnum.RUNNING)
                        .message(String.format("the status of %s is %s, not finished",
                                componentName, componentInstanceStatus.toString()))
                        .build()
            default:
                return GetDeployComponentHandlerRes.builder()
                        .status(DeployComponentStateEnum.WAIT_FOR_OP)
                        .message(String.format("the status of %s is %s, conditions=%s",
                                componentName, componentInstanceStatus.toString(), componentInstance.getConditions()))
                        .build()
        }

        // 获取 conditions 中的 dataOutput (如果有的话)
        def conditions = componentInstance.getConditions()
        if (StringUtils.isEmpty(conditions)) {
            return GetDeployComponentHandlerRes.builder()
                    .status(DeployComponentStateEnum.SUCCESS)
                    .message(String.format("the status of %s is RUNNING now", componentName))
                    .build()
        }
        def conditionsArray = JSONArray.parseArray(conditions)
        def message = String.format("the status of %s is %s now|statusDetail=%s",
                componentName, componentInstanceStatus.toString(), componentInstance.getConditions())
        for (JSONObject condition : conditionsArray.toJavaList(JSONObject.class)) {
            if (ConditionUtil.TYPE_DATA_OUTPUTS != condition.getString("type")) {
                continue
            }
            def reason = condition.getString("reason")
            if (StringUtils.isEmpty(reason)) {
                message = "cannot find reason (dataOutputs) in conditions, skip"
                continue
            }
            def dataOutputs = JSONObject.parseObject(condition.getString("reason"))
            ((JSONObject) componentSchema.getSpec().getWorkload().getSpec()).put("dataOutputs", dataOutputs)
            message = String.format("found dataOutputs in conditions, and the status of %s is RUNNING now",
                    componentName)
            break
        }
        return GetDeployComponentHandlerRes.builder()
                .status(DeployComponentStateEnum.SUCCESS)
                .componentSchema(componentSchema)
                .message(message)
                .build()
    }
}
