package com.alibaba.tesla.appmanager.workflow.action.impl.workflowtask;

import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskStateEnum;
import com.alibaba.tesla.appmanager.workflow.action.WorkflowTaskStateAction;
import com.alibaba.tesla.appmanager.workflow.event.loader.WorkflowTaskStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service("RunningSuspendWorkflowTaskStateAction")
public class RunningSuspendWorkflowTaskStateAction implements WorkflowTaskStateAction, ApplicationRunner {

    private static final WorkflowTaskStateEnum STATE = WorkflowTaskStateEnum.RUNNING_SUSPEND;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        publisher.publishEvent(new WorkflowTaskStateActionLoadedEvent(
                this, STATE.toString(), this.getClass().getSimpleName()));
    }

    /**
     * 自身处理逻辑
     *
     * @param instance Workflow 实例
     */
    @Override
    public void run(WorkflowTaskDO instance) {
        log.info(STATE.toString());
    }
}
