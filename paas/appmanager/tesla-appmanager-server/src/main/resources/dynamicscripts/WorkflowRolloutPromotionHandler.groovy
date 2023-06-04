package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.api.provider.WorkflowInstanceProvider
import com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant
import com.alibaba.tesla.appmanager.common.constants.WorkflowContextKeyConstant
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.common.service.StreamLogService
import com.alibaba.tesla.appmanager.domain.option.WorkflowInstanceOption
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
    public static final Integer REVISION = 8

    @Autowired
    private WorkflowInstanceProvider workflowInstanceProvider

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
            streamLogService.info(streamKey, "start execute WorkflowRolloutPromotionHandler")
            def context = request.getContext()
            if (context.getBooleanValue(WorkflowContextKeyConstant.QUIET_MODE)) {
                // 安静运行模式下直接继续进行部署
                log.info("continue rollout progress because of quiet mode|workflowInstanceId={}|workflowTaskId={}|" +
                        "appId={}", request.getInstanceId(), request.getTaskId(), request.getAppId())
                streamLogService.info(streamKey, "continue rollout progress because of quiet mode")
                return ExecuteWorkflowHandlerRes.builder()
                        .context(context)
                        .configuration(request.getConfiguration())
                        .build()
            }

            def continueRollout = context.getBoolean("continueRollout")
            def rollback = context.getBoolean("rollback")
            if (continueRollout == null || rollback == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "continueRollout/rollback must be set")
            }

            // 不需要回滚时
            if (!rollback) {
                if (continueRollout) {
                    log.info("continue rollout progress|workflowInstanceId={}|workflowTaskId={}|appId={}",
                            request.getInstanceId(), request.getTaskId(), request.getAppId())
                    streamLogService.info(streamKey, "continue rollout progress")
                    return ExecuteWorkflowHandlerRes.builder()
                            .context(context)
                            .configuration(request.getConfiguration())
                            .build()
                } else {
                    log.info("user terminated the current rollout progress|workflowInstanceId={}|workflowTaskId={}|" +
                            "appId={}", request.getInstanceId(), request.getTaskId(), request.getAppId())
                    streamLogService.info(streamKey, "user terminated the current rollout progress")
                    return ExecuteWorkflowHandlerRes.builder()
                            .context(context)
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
            if (targetInstance == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot find previous successful workflow instance by app %s. " +
                                "so rollback current workflow instance is not feasible", appId));
            }

            // 回滚时的 workflow context 设置为安静模式，不需要人工确认
            def rollbackContext = new JSONObject()
            rollbackContext.put(WorkflowContextKeyConstant.QUIET_MODE, true)
            def rollbackOption = WorkflowInstanceOption.builder()
                    .category("ROLLBACK")
                    .creator(request.getCreator())
                    .initContext(rollbackContext)
                    .build()
            def rollbackConfiguration = targetInstance.getWorkflowConfiguration()
            def rollbackWorkflowInstanceId = WorkflowHandlerUtil
                    .deployWorkflow(appId, rollbackConfiguration, rollbackOption)
            log.info("rollback request has applied|rollbackWorkflowInstanceId={}|workflowInstanceId={}|" +
                    "workflowTaskId={}|appId={}|rollbackConfiguration={}", rollbackWorkflowInstanceId,
                    request.getInstanceId(), request.getTaskId(), request.getAppId(),
                    JSONObject.toJSONString(rollbackConfiguration))
            streamLogService.info(streamKey, "rollback request has applied")
            context.put(WorkflowContextKeyConstant.CANCEL_EXECUTION, true)
            context.put(WorkflowContextKeyConstant.CANCEL_EXECUTION_REASON,
                    String.format("rollback succeed, and current workflow has turned into TERMINATED status"))
            return ExecuteWorkflowHandlerRes.builder()
                    .deployWorkflowInstanceId(rollbackWorkflowInstanceId)
                    .context(context)
                    .configuration(request.getConfiguration())
                    .build()
        } catch (Exception ex) {
            streamLogService.info(streamKey, ExceptionUtils.getStackTrace(ex))
            throw ex;
        } finally {
            streamLogService.info(streamKey, "execute WorkflowRolloutPromotionHandler finish!")
            streamLogService.clean(streamKey,true);
        }
    }
}
