package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.service.StreamLogService
import com.alibaba.tesla.appmanager.domain.req.workflow.ExecutePolicyHandlerReq
import com.alibaba.tesla.appmanager.domain.req.workflow.ExecuteWorkflowHandlerReq
import com.alibaba.tesla.appmanager.domain.res.workflow.ExecuteWorkflowHandlerRes
import com.alibaba.tesla.appmanager.workflow.dynamicscript.WorkflowHandler
import com.alibaba.tesla.appmanager.workflow.util.WorkflowHandlerUtil
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Workflow Default Deploy Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
class WorkflowDeployHandler implements WorkflowHandler {

    private static final Logger log = LoggerFactory.getLogger(WorkflowDeployHandler.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.WORKFLOW.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "deploy"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 5

    @Autowired
    private StreamLogService streamLogService

    /**
     * 执行逻辑
     * @param request Workflow 执行请求
     * @return Workflow 执行结果
     */
    @Override
    ExecuteWorkflowHandlerRes execute(ExecuteWorkflowHandlerReq request) throws InterruptedException {
        def streamKey = String.format("%s_%s", RedisKeyConstant.WORKFLOW_TASK_LOG,
                request.getTaskId())
        try {
            streamLogService.info(streamKey, "start execute WorkflowDeployHandler")
            def configuration = request.getConfiguration()
            def context = request.getContext()
            def policies = request.getTaskProperties().getJSONArray("policies")
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
                    streamLogService.info(streamKey, String.format("preapre to execute policy in workflow task|" +
                            "workflowInstanceId=%s|workflowTaskId=%s|" +
                            "appId=%s|context=%s|configuration=%s", request.getInstanceId(), request.getTaskId(),
                            request.getAppId(), JSONObject.toJSONString(context), JSONObject.toJSONString(configuration)), log)
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
                    streamLogService.info(streamKey, "policy has exeucted in workflow task")
                }
            }
            def deployAppId = WorkflowHandlerUtil.deploy(configuration, null, request.getCreator())
            log.info("deploy request has applied|workflowInstanceId={}|workflowTaskId={}|appId={}|context={}|" +
                    "configuration={}", request.getInstanceId(), request.getTaskId(), request.getAppId(),
                    JSONObject.toJSONString(context), JSONObject.toJSONString(configuration))
            streamLogService.info(streamKey, "deploy request has applied")
            return ExecuteWorkflowHandlerRes.builder()
                    .deployAppId(deployAppId)
                    .context(context)
                    .configuration(configuration)
                    .build()
        } catch (Exception ex) {
            streamLogService.info(streamKey, ExceptionUtils.getStackTrace(ex))
            throw ex;
        } finally {
            streamLogService.info(streamKey, "execute WorkflowDeployHandler finish!")
            streamLogService.clean(streamKey, true);
        }
    }
}
