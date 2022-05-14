package com.alibaba.tesla.appmanager.workflow.service.thread;

import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;

/**
 * 执行 Workflow Task 任务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class ExecuteWorkflowTask implements Runnable {

    private final WorkflowTaskDO task;

    public ExecuteWorkflowTask(WorkflowTaskDO task) {
        this.task = task;
    }

    @Override
    public void run() {

    }
}