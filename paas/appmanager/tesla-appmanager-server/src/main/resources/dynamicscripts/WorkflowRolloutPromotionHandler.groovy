package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.api.provider.WorkflowInstanceProvider
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant
import com.alibaba.tesla.appmanager.common.constants.WorkflowContextKeyConstant
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.common.util.SchemaUtil
import com.alibaba.tesla.appmanager.domain.req.workflow.ExecuteWorkflowHandlerReq
import com.alibaba.tesla.appmanager.domain.res.workflow.ExecuteWorkflowHandlerRes
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema
import com.alibaba.tesla.appmanager.workflow.dynamicscript.WorkflowHandler
import com.alibaba.tesla.appmanager.workflow.util.WorkflowHandlerUtil
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Workflow Rollout Promotion Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
class WorkflowRolloutPromotionHandler implements WorkflowHandler {

    private static final Logger log = LoggerFactory.getLogger(WorkflowRolloutPromotionHandler.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.WORKFLOW.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "rollout-promotion"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 2

    @Autowired
    private WorkflowInstanceProvider workflowInstanceProvider

    /**
     * 执行逻辑
     * @param request Workflow 执行请求
     * @return Workflow 执行结果
     */
    @Override
    ExecuteWorkflowHandlerRes execute(ExecuteWorkflowHandlerReq request) throws InterruptedException {
        def continueRollout = request.getContext().getBoolean("continueRollout")
        def rollback = request.getContext().getBoolean("rollback")
        if (continueRollout == null || rollback == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "continueRollout/rollback must be set")
        }

        // 不需要回滚时
        if (!rollback) {
            if (continueRollout) {
                return ExecuteWorkflowHandlerRes.builder()
                        .context(request.getContext())
                        .configuration(request.getConfiguration())
                        .build()
            } else {
                return ExecuteWorkflowHandlerRes.builder()
                        .context(request.getContext())
                        .configuration(request.getConfiguration())
                        .terminate(true)
                        .build()
            }
        }

        // 需要回滚
        if (continueRollout) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "continueRollout must be false if rollback")
        }
        def appId = request.getAppId()
        def targetInstance = workflowInstanceProvider.getLastSuccessInstance(appId, "DEPLOY")
        def configuration = SchemaUtil.toSchema(DeployAppSchema.class, targetInstance.getWorkflowConfiguration())
        def deployAppId = WorkflowHandlerUtil.deploy(configuration, null, request.getCreator())
        log.info("rollback request has applied|deployAppId={}|workflowInstanceId={}|workflowTaskId={}|appId={}|" +
                "configuration={}", deployAppId, request.getInstanceId(), request.getTaskId(),
                request.getAppId(), JSONObject.toJSONString(configuration))
        def context = request.getContext()
        context.put(WorkflowContextKeyConstant.CANCEL_EXECUTION, true)
        return ExecuteWorkflowHandlerRes.builder()
                .deployAppId(deployAppId)
                .context(context)
                .configuration(request.getConfiguration())
                .build()
    }
}
