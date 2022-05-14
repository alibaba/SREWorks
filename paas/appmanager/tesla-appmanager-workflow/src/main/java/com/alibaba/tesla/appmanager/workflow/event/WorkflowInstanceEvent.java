package com.alibaba.tesla.appmanager.workflow.event;

import com.alibaba.tesla.appmanager.common.enums.WorkflowInstanceEventEnum;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Workflow Instance 事件基类
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class WorkflowInstanceEvent extends ApplicationEvent {

    @Getter
    protected WorkflowInstanceEventEnum event;

    @Getter
    protected Long workflowInstanceId;

    public WorkflowInstanceEvent(Object source, WorkflowInstanceEventEnum event, Long workflowInstanceId) {
        super(source);
        this.event = event;
        this.workflowInstanceId = workflowInstanceId;
    }
}
