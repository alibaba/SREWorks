package com.alibaba.tesla.appmanager.workflow.action.impl.workflowtask;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.DeployAppProvider;
import com.alibaba.tesla.appmanager.api.provider.UnitProvider;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskStateEnum;
import com.alibaba.tesla.appmanager.domain.dto.DeployAppDTO;
import com.alibaba.tesla.appmanager.domain.req.deploy.DeployAppGetReq;
import com.alibaba.tesla.appmanager.domain.req.deploy.DeployAppTerminateReq;
import com.alibaba.tesla.appmanager.workflow.action.WorkflowTaskStateAction;
import com.alibaba.tesla.appmanager.workflow.event.loader.WorkflowTaskStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service("TerminatedWorkflowTaskStateAction")
public class TerminatedWorkflowTaskStateAction implements WorkflowTaskStateAction, ApplicationRunner {

    private static final WorkflowTaskStateEnum STATE = WorkflowTaskStateEnum.TERMINATED;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    @Autowired
    private UnitProvider unitProvider;

    @Autowired
    private DeployAppProvider deployAppProvider;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        publisher.publishEvent(new WorkflowTaskStateActionLoadedEvent(
                this, STATE.toString(), this.getClass().getSimpleName()));
    }

    /**
     * 自身处理逻辑
     *
     * @param task Workflow 实例
     */
    @Override
    public void run(WorkflowTaskDO task) {
        log.info("the current workflow task enters the TERMINATED state|workflowInstanceId={}|workflowTaskId={}",
                task.getWorkflowInstanceId(), task.getId());
        WorkflowInstanceDO instance = workflowInstanceService.get(task.getWorkflowInstanceId(), true);
        Long deployWorkflowInstanceId = task.getDeployWorkflowInstanceId();
        Long deployAppId = task.getDeployAppId();
        String deployAppUnitId = task.getDeployAppUnitId();
        String deployAppNamespaceId = task.getDeployAppNamespaceId();
        String deployAppStageId = task.getDeployAppStageId();

        // 如果当前实例存在运行中的 deploy app 部署单，需要进行二次 termianted
        if (deployAppId != null && deployAppId > 0) {
            if (StringUtils.isNotEmpty(deployAppUnitId)
                    || StringUtils.isNotEmpty(deployAppNamespaceId)
                    || StringUtils.isNotEmpty(deployAppStageId)) {
                unitProvider.terminateDeployment(deployAppUnitId,
                        DeployAppTerminateReq.builder().deployAppId(deployAppId).build());
                log.info("find remote deploy app records, terminated|workflowInstanceId={}|workflowTaskId={}|" +
                        "deployAppId={}|deployAppUnitId={}|deployAppNamespaceId={}|deployAppStageId={}",
                        task.getWorkflowInstanceId(), task.getId(), deployAppId, deployAppUnitId, deployAppNamespaceId,
                        deployAppStageId);
            } else {
                DeployAppTerminateReq request = DeployAppTerminateReq.builder()
                        .deployAppId(deployAppId)
                        .build();
                deployAppProvider.terminate(request, instance.getWorkflowCreator());
                log.info("find local deploy app records, terminated|workflowInstanceId={}|workflowTaskId={}|" +
                                "deployAppId={}", task.getWorkflowInstanceId(), task.getId(), deployAppId);
            }
        }

        // TODO: 如果当前实例存在运行中的 workflow 实例，需要进行二次 terminated

        if (StringUtils.isNotEmpty(task.getTaskErrorMessage())) {
            workflowInstanceService.triggerOpTerminate(instance, task.getTaskErrorMessage());
        } else {
            workflowInstanceService.triggerOpTerminate(instance, "terminated");
        }
    }
}
