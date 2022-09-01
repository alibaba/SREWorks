package com.alibaba.tesla.appmanager.plugin.repository;

import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginResourceWorkflowRelQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceWorkflowRelDO;

import java.util.List;

public interface PluginResourceWorkflowRelRepository {

    long countByCondition(PluginResourceWorkflowRelQueryCondition condition);

    int deleteByCondition(PluginResourceWorkflowRelQueryCondition condition);

    int insert(PluginResourceWorkflowRelDO record);

    List<PluginResourceWorkflowRelDO> selectByCondition(PluginResourceWorkflowRelQueryCondition condition);

    PluginResourceWorkflowRelDO getByCondition(PluginResourceWorkflowRelQueryCondition condition);

    int updateByCondition(PluginResourceWorkflowRelDO record, PluginResourceWorkflowRelQueryCondition condition);
}
