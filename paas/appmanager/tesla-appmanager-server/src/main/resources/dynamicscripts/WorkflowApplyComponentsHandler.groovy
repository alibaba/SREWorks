package dynamicscripts

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.constants.WorkflowContextKeyConstant
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.domain.container.DeployAppRevisionName
import com.alibaba.tesla.appmanager.domain.req.workflow.ExecutePolicyHandlerReq
import com.alibaba.tesla.appmanager.domain.req.workflow.ExecuteWorkflowHandlerReq
import com.alibaba.tesla.appmanager.domain.res.workflow.ExecuteWorkflowHandlerRes
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema
import com.alibaba.tesla.appmanager.workflow.dynamicscript.WorkflowHandler
import com.alibaba.tesla.appmanager.workflow.util.WorkflowHandlerUtil
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.SerializationUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

/**
 * Workflow 应用组件 Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
class WorkflowApplyComponentsHandler implements WorkflowHandler {

    private static final Logger log = LoggerFactory.getLogger(WorkflowApplyComponentsHandler.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.WORKFLOW.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "apply-components"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 3

    /**
     * 执行逻辑
     * @param request Workflow 执行请求
     * @return Workflow 执行结果
     */
    @Override
    ExecuteWorkflowHandlerRes execute(ExecuteWorkflowHandlerReq request) throws InterruptedException {
        def configuration = request.getConfiguration()
        def context = request.getContext()
        def properties = request.getTaskProperties()
        def policies = properties.getJSONArray("policies")
        def components = properties.getJSONArray("components")
        def rollout = properties.getJSONObject("rollout")

        // 判断是否有取消执行的标志位，那么直接置为 terminate
        if (context.getBooleanValue(WorkflowContextKeyConstant.CANCEL_EXECUTION)) {
            return ExecuteWorkflowHandlerRes.builder()
                    .context(context)
                    .configuration(request.getConfiguration())
                    .terminate(true)
                    .build()
        }

        if (policies != null && policies.size() > 0) {
            for (def policyName : policies.toJavaList(String.class)) {
                def policy = WorkflowHandlerUtil.getPolicy(configuration, policyName)
                def policyHandler = WorkflowHandlerUtil.getPolicyHandler(policy.getType())
                def policyProperties = policy.getProperties()
                def req = ExecutePolicyHandlerReq.builder()
                        .appId(request.getAppId())
                        .instanceId(request.getInstanceId())
                        .taskId(request.getTaskId())
                        .policyProperties(policyProperties)
                        .context(context)
                        .configuration(configuration)
                        .build()
                log.info("preapre to execute policy in workflow task|workflowInstanceId={}|workflowTaskId={}|" +
                        "appId={}|context={}|configuration={}", request.getInstanceId(), request.getTaskId(),
                        request.getAppId(), JSONObject.toJSONString(context), JSONObject.toJSONString(configuration))
                def res = policyHandler.execute(req)
                if (res.getContext() != null) {
                    context = res.getContext();
                }
                if (res.getConfiguration() != null) {
                    configuration = res.getConfiguration();
                }
                log.info("policy has exeucted in workflow task|workflowInstanceId={}|workflowTaskId={}|appId={}|" +
                        "context={}|configuration={}", request.getInstanceId(), request.getTaskId(), request.getAppId(),
                        JSONObject.toJSONString(context), JSONObject.toJSONString(configuration))
            }
        }

        // 根据 components 中的配置创建新的局部 Application Configuration
        def newConfiguration = generateConfiguration(configuration, components, rollout)
        def overwriteParameters = context.getJSONObject(WorkflowContextKeyConstant.DEPLOY_OVERWRITE_PARAMETERS)
        def deployAppId = WorkflowHandlerUtil.deploy(newConfiguration, overwriteParameters, request.getCreator())
        log.info("deploy request has applied|workflowInstanceId={}|workflowTaskId={}|appId={}|context={}|" +
                "configuration={}", request.getInstanceId(), request.getTaskId(), request.getAppId(),
                JSONObject.toJSONString(context), JSONObject.toJSONString(newConfiguration))
        return ExecuteWorkflowHandlerRes.builder()
                .deployAppId(deployAppId)
                .context(context)
                .configuration(newConfiguration)
                .build()
    }

    /**
     * 根据 apply-components 中配置的内容来生成 Configuration
     * @param configuration 原始 Configuration
     * @param components 要部署的目标组件
     * @param rolloutObj 发布策略
     * @return 重新生成的 Application Configuration
     */
    static DeployAppSchema generateConfiguration(
            DeployAppSchema configuration, JSONArray components, JSONObject rolloutObj) {
        def keySet = new HashSet<String>()
        for (def component : components.toJavaList(JSONObject.class)) {
            def type = component.getString("type")
            def name = component.getString("name")
            if (StringUtils.isAnyEmpty(type, name)) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "type/name are required in 'components' field")
            }
            keySet.add(keyGenerator(type, name))
        }

        // 组装新的 Application Configuration
        def newConfiguration = SerializationUtils.clone(configuration)
        newConfiguration.getSpec().setComponents(newConfiguration.getSpec().getComponents()
                .stream()
                .filter(item -> {
                    def name = DeployAppRevisionName.valueOf(item.getRevisionName())
                    return keySet.contains(keyGenerator(name.getComponentType(), name.getComponentName()))
                })
                .collect(Collectors.toList()))
        // 清理并重建依赖关系 (顺次执行)
        def previousComponent = ""
        for (def specComponent : newConfiguration.getSpec().getComponents()) {
            if (StringUtils.isNotEmpty(previousComponent)) {
                specComponent.setDependencies(Arrays.asList(DeployAppSchema.Dependency.builder()
                        .component(previousComponent)
                        .build()))
            } else {
                specComponent.setDependencies(new ArrayList<>())
                previousComponent = dependencyKeyGenerator(specComponent.getRevisionName())
            }
        }

        // 如果存在 rollout, 那么放入各个组件的 ComponentSchema 中
        if (rolloutObj == null) {
            return newConfiguration
        }
        def type = rolloutObj.getString("type")
        if (type != "partition") {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "only partition type is supported now|rollout=" + JSONObject.toJSONString(rolloutObj))
        }
        for (def specComponent : newConfiguration.getSpec().getComponents()) {
            if (specComponent.getParameterValues() == null) {
                specComponent.setParameterValues(new ArrayList<>())
            }
            specComponent.getParameterValues().add(DeployAppSchema.ParameterValue.builder()
                    .name("rollout")
                    .value(rolloutObj)
                    .toFieldPaths(Arrays.asList("spec.rollout"))
                    .build())
        }
        return newConfiguration
    }

    /**
     * Key Generator
     * @param type 类型
     * @param name 名称
     * @return
     */
    static String keyGenerator(String type, String name) {
        return String.format("%s_%s", type, name)
    }

    /**
     * Dependency Key Generator
     * @param revisionName Revision Name
     * @return
     */
    static String dependencyKeyGenerator(String revisionName) {
        def revision = DeployAppRevisionName.valueOf(revisionName)
        return String.format("%s|%s", revision.getComponentType(), revision.getComponentName())
    }
}
