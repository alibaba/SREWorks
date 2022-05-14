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
}