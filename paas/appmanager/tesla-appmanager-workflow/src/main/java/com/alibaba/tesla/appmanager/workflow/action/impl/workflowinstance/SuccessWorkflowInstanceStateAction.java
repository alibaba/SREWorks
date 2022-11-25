package com.alibaba.tesla.appmanager.workflow.action.impl.workflowinstance;

import com.alibaba.tesla.appmanager.api.provider.WorkflowTaskProvider;
import com.alibaba.tesla.appmanager.common.enums.WorkflowInstanceStateEnum;
import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskEventEnum;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.WorkflowTaskDTO;
import com.alibaba.tesla.appmanager.domain.req.workflow.WorkflowTaskListReq;
import com.alibaba.tesla.appmanager.workflow.action.WorkflowInstanceStateAction;
import com.alibaba.tesla.appmanager.workflow.event.WorkflowTaskEvent;
import com.alibaba.tesla.appmanager.workflow.event.loader.WorkflowInstanceStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service("SuccessWorkflowInstanceStateAction")
public class SuccessWorkflowInstanceStateAction implements WorkflowInstanceStateAction, ApplicationRunner {

    private static final WorkflowInstanceStateEnum STATE = WorkflowInstanceStateEnum.SUCCESS;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private WorkflowTaskProvider workflowTaskProvider;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        publisher.publishEvent(new WorkflowInstanceStateActionLoadedEvent(
                this, STATE.toString(), this.getClass().getSimpleName()));
    }

    /**
     * 自身处理逻辑
     *
     * @param instance Workflow 实例
     */
    @Override
    public void run(WorkflowInstanceDO instance) {
        log.info("the current workflow instance has entered the SUCCESS state|workflowInstanceId={}|appId={}",
                instance.getId(), instance.getAppId());

        // 如果 workflow 中存在关联项，那么发送事件，触发工作流继续进行
        WorkflowTaskListReq req = WorkflowTaskListReq.builder()
                .deployWorkflowInstanceId(instance.getId())
                .build();
        Pagination<WorkflowTaskDTO> workflowTasks = workflowTaskProvider.list(req);
        if (workflowTasks.getItems().size() == 0) {
            log.info("no associated workflow tasks found, skip|deployWorkflowInstanceId={}", instance.getId());
            return;
        }

        for (WorkflowTaskDTO item : workflowTasks.getItems()) {
            log.info("find associated workflow task, publish TRIGGER_UPDATE to it|workflowInstanceId={}|" +
                            "workflowTaskId={}|deployWorkflowInstanceId={}|deployWorkflowInstanceStatus={}",
                    item.getWorkflowInstanceId(), item.getId(), instance.getId(), instance.getWorkflowStatus());
            publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.TRIGGER_UPDATE, item));
        }
    }
}
