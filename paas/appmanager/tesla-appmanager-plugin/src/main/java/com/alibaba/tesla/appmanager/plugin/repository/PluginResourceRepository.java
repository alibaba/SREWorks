package com.alibaba.tesla.appmanager.plugin.repository;

import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginResourceQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceDO;

import java.util.List;

public interface PluginResourceRepository {

    long countByCondition(PluginResourceQueryCondition condition);

    int deleteByCondition(PluginResourceQueryCondition condition);

    int insert(PluginResourceDO record);

    List<PluginResourceDO> selectByCondition(PluginResourceQueryCondition condition);

    PluginResourceDO getByCondition(PluginResourceQueryCondition condition);

    int updateByCondition(PluginResourceDO record, PluginResourceQueryCondition condition);
}
