package dynamicscripts


import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.ComponentInstanceStatusEnum
import com.alibaba.tesla.appmanager.common.enums.DeployComponentAttrTypeEnum
import com.alibaba.tesla.appmanager.common.enums.DeployComponentStateEnum
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.util.ConditionUtil
import com.alibaba.tesla.appmanager.common.util.SchemaUtil
import com.alibaba.tesla.appmanager.domain.req.rtcomponentinstance.RtComponentInstanceGetStatusReq
import com.alibaba.tesla.appmanager.domain.res.rtcomponentinstance.RtComponentInstanceGetStatusRes
import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema
import com.alibaba.tesla.appmanager.dynamicscript.core.GroovyHandlerFactory
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory
import com.alibaba.tesla.appmanager.server.dynamicscript.handler.ComponentCustomStatusHandler
import com.alibaba.tesla.appmanager.server.dynamicscript.handler.ComponentWatchCronHandler
import com.alibaba.tesla.appmanager.server.repository.condition.DeployComponentQueryCondition
import com.alibaba.tesla.appmanager.server.service.deploy.DeployAppService
import com.alibaba.tesla.appmanager.server.service.deploy.DeployComponentService
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Script Component 类型组件状态获取 Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j(topic = "status")
class ScriptComponentWatchCronHandler implements ComponentWatchCronHandler {

    private static final Logger log = LoggerFactory.getLogger(ScriptComponentWatchCronHandler.class)

    /**
     * Handler 元信息
     */
    public static final String KIND = DynamicScriptKindEnum.COMPONENT_WATCH_CRON.toString()
    public static final String NAME = "script.component.sreworks.io/v1beta1/watch"
    public static final Integer REVISION = 7

    @Autowired
    private DeployAppService deployAppService

    @Autowired
    private DeployComponentService deployComponentService

    @Autowired
    private KubernetesClientFactory kubernetesClientFactory

    @Autowired
    private GroovyHandlerFactory groovyHandlerFactory

    /**
     * 获取指定组件实例的当前状态信息
     *
     * @return 当前该组件实例状态
     */
    @Override
    RtComponentInstanceGetStatusRes get(RtComponentInstanceGetStatusReq request) {
        def appId = request.getAppId()
        def clusterId = request.getClusterId()
        def namespaceId = request.getNamespaceId()
        def stageId = request.getStageId()
        def componentName = request.getComponentName()
        def logSuffix = String.format("appId=%s|clusterId=%s|namespaceId=%s|stageId=%s|componentName=%s", appId,
                clusterId, namespaceId, stageId, componentName)

        // 检测最后一次组件部署单是否存在 (PROCESSING/RUNNING -> 当前运行中; SUCCESS -> 历史最后一次成功)
        def identifierPrefix = "SCRIPT|" + componentName
        def deployComponents = deployComponentService.list(DeployComponentQueryCondition.builder()
                .appId(request.getAppId())
                .clusterId(request.getClusterId())
                .namespaceId(request.getNamespaceId())
                .stageId(request.getStageId())
                .identifierStartsWith(identifierPrefix)
                .pageSize(1)
                .deployStatusList(Arrays.asList(DeployComponentStateEnum.RUNNING,
                        DeployComponentStateEnum.PROCESSING, DeployComponentStateEnum.SUCCESS))
                .build(), true)
        // 针对于第一次部署场景，还没有成功记录，那么允许获取失败的记录
        if (deployComponents.size() == 0) {
            deployComponents = deployComponentService.list(DeployComponentQueryCondition.builder()
                    .appId(request.getAppId())
                    .clusterId(request.getClusterId())
                    .namespaceId(request.getNamespaceId())
                    .stageId(request.getStageId())
                    .identifierStartsWith(identifierPrefix)
                    .pageSize(1)
                    .build(), true)
        }
        if (deployComponents.size() == 0) {
            log.warn("current component is not existed|{}", logSuffix)
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.EXPIRED.toString())
                    .conditions(ConditionUtil.singleCondition("CheckScriptComponentExisted", "False",
                            "current component is not existed in the records of deployments", ""))
                    .build()
        }

        // 转换 ComponentSchema 回来，获取 kubeconfig 及 options
        def componentSchemaYaml = deployComponents.get(0).getAttrMap()
                .get(DeployComponentAttrTypeEnum.COMPONENT_SCHEMA.toString())
        log.debug("get component schema yaml from deploy component attr map|{}|yaml={}", logSuffix, componentSchemaYaml)
        def componentSchema = SchemaUtil.toSchema(ComponentSchema.class, componentSchemaYaml)
        def workloadSpec = (JSONObject) componentSchema.getSpec().getWorkload().getSpec()
        def scriptName = workloadSpec.getString("scriptName")
        def base64Kubeconfig = workloadSpec.getString("base64Kubeconfig")
        def options = workloadSpec.getJSONObject("options")
        if (StringUtils.isEmpty(scriptName)) {
            def errorMessage = "empty scriptName variable in workload spec"
            log.warn("{}|{}", errorMessage, logSuffix)
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.FAILED.toString())
                    .conditions(ConditionUtil.singleCondition("GetScriptName", "False", errorMessage, ""))
                    .build()
        }
        if (StringUtils.isEmpty(base64Kubeconfig)) {
            def errorMessage = "empty base64Kubeconfig variable in workload spec"
            log.warn("{}|{}", errorMessage, logSuffix)
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.FAILED.toString())
                    .conditions(ConditionUtil.singleCondition("GetKubeconfig", "False", errorMessage, ""))
                    .build()
        }
        def client = kubernetesClientFactory.getByKubeConfig(base64Kubeconfig)
        def handler = groovyHandlerFactory.get(ComponentCustomStatusHandler.class,
                DynamicScriptKindEnum.CUSTOM_STATUS.toString(), scriptName)
        try {
            return handler.getStatus(request, client, options)
        } catch (Exception e) {
            return RtComponentInstanceGetStatusRes.builder()
                    .status(ComponentInstanceStatusEnum.UNKNOWN.toString())
                    .conditions(ConditionUtil.singleCondition("GetStatusInScript", "False",
                            ExceptionUtils.getStackTrace(e), ""))
                    .build()
        }
    }
}