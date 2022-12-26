package com.alibaba.tesla.appmanager.workflow.repository;

import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowTaskQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;

import java.util.List;

public interface WorkflowTaskRepository {

    long countByCondition(WorkflowTaskQueryCondition condition);

    int deleteByCondition(WorkflowTaskQueryCondition condition);

    int insert(WorkflowTaskDO record);

    List<WorkflowTaskDO> selectByCondition(WorkflowTaskQueryCondition condition);

    WorkflowTaskDO getByCondition(WorkflowTaskQueryCondition condition);

    int updateByCondition(WorkflowTaskDO record, WorkflowTaskQueryCondition condition);

    int updateByPrimaryKey(WorkflowTaskDO record);

    /**
     * 获取指定 workflowInstance 中剩余的 PENDING 待运行任务
     *
     * @param workflowInstanceId Workflow Instance ID
     * @return 待运行 Workflow 任务
     */
    List<WorkflowTaskDO> notFinishedTasks(Long workflowInstanceId);

    /**
     * 列出当前所有正在运行中的远程 workflow task
     *
     * @return List or WorkflowTaskDO
     */
    List<WorkflowTaskDO> listRunningRemoteTask();
}