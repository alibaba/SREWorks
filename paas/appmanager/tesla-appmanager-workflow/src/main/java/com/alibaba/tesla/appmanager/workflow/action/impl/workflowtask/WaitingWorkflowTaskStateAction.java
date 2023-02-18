package com.alibaba.tesla.appmanager.workflow.action.impl.workflowtask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.DeployAppProvider;
import com.alibaba.tesla.appmanager.api.provider.UnitProvider;
import com.alibaba.tesla.appmanager.common.constants.AppFlowParamKey;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.constants.WorkflowContextKeyConstant;
import com.alibaba.tesla.appmanager.common.enums.*;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.dto.DeployAppAttrDTO;
import com.alibaba.tesla.appmanager.domain.dto.DeployAppDTO;
import com.alibaba.tesla.appmanager.domain.req.UpdateWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.domain.req.deploy.DeployAppGetAttrReq;
import com.alibaba.tesla.appmanager.domain.req.deploy.DeployAppGetReq;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.appmanager.workflow.action.WorkflowTaskStateAction;
import com.alibaba.tesla.appmanager.workflow.event.WorkflowTaskEvent;
import com.alibaba.tesla.appmanager.workflow.event.loader.WorkflowTaskStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowSnapshotQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowInstanceService;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowSnapshotService;
import com.google.common.base.Enums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.stereotype.Service;

import javax.ws.rs.HEAD;
import java.util.List;

@Slf4j
@Service("WaitingWorkflowTaskStateAction")
public class WaitingWorkflowTaskStateAction implements WorkflowTaskStateAction, ApplicationRunner {

    private static final WorkflowTaskStateEnum STATE = WorkflowTaskStateEnum.WAITING;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private DeployAppProvider deployAppProvider;

    @Autowired
    private UnitProvider unitProvider;

    @Autowired
    private WorkflowSnapshotService workflowSnapshotService;

    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        publisher.publishEvent(new WorkflowTaskStateActionLoadedEvent(
                this, STATE.toString(), this.getClass().getSimpleName()));
    }

    /**
     * 自身处理逻辑
     * <p>
     * 注意此处 WAITING 的事件由 DeployAppService 在完成时主动触发
     *
     * @param task Workflow 实例
     */
    @Override
    public void run(WorkflowTaskDO task) {
        Long deployWorkflowInstanceId = task.getDeployWorkflowInstanceId();
        Long deployAppId = task.getDeployAppId();
        String deployAppUnitId = task.getDeployAppUnitId();
        String deployAppNamespaceId = task.getDeployAppNamespaceId();
        String deployAppStageId = task.getDeployAppStageId();

        // 增加需要 deliver 到后续节点的参数内容
        enrichContextByDeliverParameterValues(task);

        if (deployAppId != null && deployAppId > 0) {
            DeployAppGetReq request = DeployAppGetReq.builder().deployAppId(deployAppId).build();
            DeployAppDTO deployApp;
            if (StringUtils.isNotEmpty(deployAppUnitId)
                    || StringUtils.isNotEmpty(deployAppNamespaceId)
                    || StringUtils.isNotEmpty(deployAppStageId)) {
                JSONObject response = unitProvider.getDeployment(deployAppUnitId,
                        DeployAppGetReq.builder().deployAppId(deployAppId).build());
                deployApp = response.toJavaObject(DeployAppDTO.class);
            } else {
                deployApp = deployAppProvider.get(request, DefaultConstant.SYSTEM_OPERATOR);
            }
            DeployAppStateEnum deployStatus = Enums
                    .getIfPresent(DeployAppStateEnum.class, deployApp.getDeployStatus()).orNull();
            if (deployStatus == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("invalid deploy app status %s for deployment %d",
                                deployApp.getDeployStatus(), deployAppId));
            }
            switch (deployStatus) {
                case SUCCESS:
                    log.info("the deployment has been completed, and the workflow task has ended waiting|" +
                                    "workflowInstanceId={}|workflowTaskId={}|deployAppId={}|deployStatus={}",
                            task.getWorkflowInstanceId(), task.getId(), deployApp.getId(),
                            deployApp.getDeployStatus());
                    task.setTaskStatus(WorkflowTaskStateEnum.SUCCESS.toString());
                    task.setTaskErrorMessage("");
                    enrichContextByOverwriteParameterValues(task);
                    publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.WAITING_FINISHED, task));
                    break;
                case FAILURE:
                case WAIT_FOR_OP:
                    log.warn("the deployment has been completed, but reached FAILURE/WAIT_FOR_OP status, and the workflow" +
                                    "task has ended waiting|workflowInstanceId={}|workflowTaskId={}|deployAppId={}|" +
                                    "deployStatus={}|errorMessage={}", task.getWorkflowInstanceId(), task.getId(),
                            deployApp.getId(), deployApp.getDeployStatus(), deployApp.getDeployErrorMessage());
                    task.setTaskStatus(WorkflowTaskStateEnum.FAILURE.toString());
                    task.setTaskErrorMessage(deployApp.getDeployErrorMessage());
                    publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.WAITING_FAILED, task));
                    break;
                case EXCEPTION:
                    log.error("the deployment has been completed, but reached EXCEPTION status, and the workflow" +
                                    "task has ended waiting|workflowInstanceId={}|workflowTaskId={}|deployAppId={}|" +
                                    "deployStatus={}|errorMessage={}", task.getWorkflowInstanceId(), task.getId(),
                            deployApp.getId(), deployApp.getDeployStatus(), deployApp.getDeployErrorMessage());
                    task.setTaskStatus(WorkflowTaskStateEnum.EXCEPTION.toString());
                    task.setTaskErrorMessage(deployApp.getDeployErrorMessage());
                    publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.UNKNOWN_ERROR, task));
                    break;
                default:
                    // 不需要触发事件，会由 DeployApp 侧自行在终态时再次触发
                    log.info("the deployment is running now, and the workflow task need to continue to wait|" +
                                    "workflowInstanceId={}|workflowTaskId={}|deployAppId={}|deployStatus={}",
                            task.getWorkflowInstanceId(), task.getId(), deployApp.getId(),
                            deployApp.getDeployStatus());
                    break;
            }
        } else if (deployWorkflowInstanceId != null && deployWorkflowInstanceId > 0) {
            WorkflowInstanceDO deployWorkflowInstance = workflowInstanceService.get(deployWorkflowInstanceId, false);
            if (deployWorkflowInstance == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("invalid deployWorkflowInstanceId returned by workflow task|" +
                                "deployWorkflowInstanceId=%d", deployWorkflowInstanceId));
            }
            WorkflowInstanceStateEnum status = Enums.getIfPresent(WorkflowInstanceStateEnum.class,
                    deployWorkflowInstance.getWorkflowStatus()).orNull();
            if (status == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("invalid deploy workflow status %s for workflow %d",
                                deployWorkflowInstance.getWorkflowStatus(), deployWorkflowInstanceId));
            }
            switch (status) {
                case SUCCESS:
                    log.info("the deploy-workflow has been completed, and the workflow task has ended waiting|" +
                                    "workflowInstanceId={}|workflowTaskId={}|newWorkflowInstanceId={}|" +
                                    "newWorkflowInstanceStatus={}", task.getWorkflowInstanceId(), task.getId(),
                            deployWorkflowInstance.getId(), deployWorkflowInstance.getWorkflowStatus());
                    task.setTaskStatus(WorkflowTaskStateEnum.SUCCESS.toString());
                    task.setTaskErrorMessage("");
                    publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.WAITING_FINISHED, task));
                    break;
                case FAILURE:
                    log.warn("the deploy-workflow has been completed, but reached FAILURE/WAIT_FOR_OP status, " +
                                    "and the workflowtask has ended waiting|workflowInstanceId={}|workflowTaskId={}|" +
                                    "newWorkflowInstanceId={}|newWorkflowInstanceStatus={}|errorMessage={}",
                            task.getWorkflowInstanceId(), task.getId(), deployWorkflowInstance.getId(),
                            deployWorkflowInstance.getWorkflowStatus(),
                            deployWorkflowInstance.getWorkflowErrorMessage());
                    task.setTaskStatus(WorkflowTaskStateEnum.FAILURE.toString());
                    task.setTaskErrorMessage(deployWorkflowInstance.getWorkflowErrorMessage());
                    publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.WAITING_FAILED, task));
                    break;
                case SUSPEND:
                case TERMINATED:
                case EXCEPTION:
                    log.error("the deploy-workflow has been completed, but reached EXCEPTION status, and the workflow" +
                                    "task has ended waiting|workflowInstanceId={}|workflowTaskId={}|" +
                                    "newWorkflowInstanceId={}|newWorkflowInstanceStatus={}|errorMessage={}",
                            task.getWorkflowInstanceId(), task.getId(), deployWorkflowInstance.getId(),
                            deployWorkflowInstance.getWorkflowStatus(),
                            deployWorkflowInstance.getWorkflowErrorMessage());
                    task.setTaskStatus(WorkflowTaskStateEnum.EXCEPTION.toString());
                    task.setTaskErrorMessage(deployWorkflowInstance.getWorkflowErrorMessage());
                    publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.UNKNOWN_ERROR, task));
                    break;
                default:
                    // 不需要触发事件，会由 Workflow 侧自行在终态时再次触发
                    log.info("the deploy-workflow is running now, and the workflow task need to continue to wait|" +
                                    "workflowInstanceId={}|workflowTaskId={}|newWorkflowInstanceId={}|" +
                                    "newWorkflowInstanceStatus={}", task.getWorkflowInstanceId(),
                            task.getId(), deployWorkflowInstance.getId(), deployWorkflowInstance.getWorkflowStatus());
                    break;
            }
        } else {
            log.info("skip workflow task waiting process|workflowInstanceId={}|workflowTaskId={}|taskStatus={}",
                    task.getWorkflowInstanceId(), task.getId(), task.getTaskStatus());
            publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.WAITING_FINISHED, task));
        }
    }

    /**
     * 将当前 workflow task 对应的 context 附加 deploy app 对应的 GLOBAL_PARAMS 中的 overwriteParameterValues 内容
     *
     * @param task Workflow Task
     */
    private void enrichContextByOverwriteParameterValues(WorkflowTaskDO task) {
        Long deployAppId = task.getDeployAppId();
        if (StringUtils.isNotEmpty(task.getDeployAppUnitId())
                || StringUtils.isNotEmpty(task.getDeployAppNamespaceId())
                || StringUtils.isNotEmpty(task.getDeployAppStageId())) {
            return;
        }

        String logSuffix = String.format("workflowInstanceId=%d|workflowTaskId=%d|deployAppId=%d",
                task.getWorkflowInstanceId(), task.getId(), deployAppId);
        log.info("prepare to enrich context by overwrite parameter values|{}", logSuffix);
        DeployAppAttrDTO deployApp = deployAppProvider.getAttr(DeployAppGetAttrReq.builder()
                .deployAppId(deployAppId)
                .build(), DefaultConstant.SYSTEM_OPERATOR);
        if (deployApp == null || deployApp.getAttrMap() == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find attrMap in deploy app|%s", logSuffix));
        }

        String globalParams = deployApp.getAttrMap().get(DeployAppAttrTypeEnum.GLOBAL_PARAMS.toString());
        if (StringUtils.isEmpty(globalParams)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find globalParams in deploy app attr|%s", logSuffix));
        }
        JSONObject parameters = JSONObject.parseObject(globalParams)
                .getJSONObject(AppFlowParamKey.OVERWRITE_PARAMETER_VALUES);
        if (parameters.size() == 0) {
            log.info("no overwrite parameter values found in workflow task|{}", logSuffix);
            return;
        }

        // 覆写 workflow task 的 context 并重新写回数据库
        JSONObject context = workflowSnapshotService.getContext(task.getWorkflowInstanceId(), task.getId());
        context.putIfAbsent(WorkflowContextKeyConstant.DEPLOY_OVERWRITE_PARAMETERS, new JSONObject());
        context.getJSONObject(WorkflowContextKeyConstant.DEPLOY_OVERWRITE_PARAMETERS).putAll(parameters);
        workflowSnapshotService.update(UpdateWorkflowSnapshotReq.builder()
                .workflowTaskId(task.getId())
                .workflowInstanceId(task.getWorkflowInstanceId())
                .context(context)
                .build());
        log.info("workflow context has been enriched by overwrite parameter values|{}|parameters={}",
                logSuffix, parameters.toJSONString());
    }

    /**
     * 将当前 workflow task 对应的 context 附加 deploy app 对应的 GLOBAL_PARAMS 中的 DeliverParameterValues 内容
     *
     * @param task Workflow Task
     */
    private void enrichContextByDeliverParameterValues(WorkflowTaskDO task) {
        Long deployAppId = task.getDeployAppId();
        String taskOutputs = task.getTaskOutputs();
        List<DeployAppSchema.WorkflowStepOutput> outputs = JSONObject
                .parseArray(taskOutputs, DeployAppSchema.WorkflowStepOutput.class);
        JSONObject deliverValue = new JSONObject();
        String logSuffix = String.format("workflowInstanceId=%d|workflowTaskId=%d|deployAppId=%d",
                task.getWorkflowInstanceId(), task.getId(), deployAppId);
        log.info("prepare to enrich context by deliver parameter values|{}", logSuffix);
        JSONObject context = workflowSnapshotService.getContext(task.getWorkflowInstanceId(), task.getId());
        DocumentContext contextStr = JsonPath.parse(JSONObject.toJSONString(context));
        for (int i = 0; i < outputs.size(); i++) {
            DeployAppSchema.WorkflowStepOutput output = outputs.get(i);
            deliverValue.put(output.getName(),
                    contextStr.read(DefaultConstant.JSONPATH_PREFIX + output.getValueFrom()));
        }

        // 覆写 workflow task 的 context 并重新写回数据库
        context.putIfAbsent(WorkflowContextKeyConstant.DEPLOY_DELIVER_PARAMETERS, new JSONObject());
        context.getJSONObject(WorkflowContextKeyConstant.DEPLOY_DELIVER_PARAMETERS).putAll(deliverValue);
        workflowSnapshotService.update(UpdateWorkflowSnapshotReq.builder()
                .workflowTaskId(task.getId())
                .workflowInstanceId(task.getWorkflowInstanceId())
                .context(context)
                .build());
        log.info("workflow context has been enriched by deliver|{}|context={}|deliverValue={}|",
                logSuffix, context.toJSONString(), deliverValue);
    }
}
