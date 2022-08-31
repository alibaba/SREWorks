package com.alibaba.tesla.appmanager.plugin.repository;

import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginDefinitionQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;

import java.util.List;

public interface PluginDefinitionRepository {

    long countByCondition(PluginDefinitionQueryCondition condition);

    int deleteByCondition(PluginDefinitionQueryCondition condition);

    int insert(PluginDefinitionDO record);

    List<PluginDefinitionDO> selectByCondition(PluginDefinitionQueryCondition condition);

    PluginDefinitionDO getByCondition(PluginDefinitionQueryCondition condition);

    int updateByCondition(PluginDefinitionDO record, PluginDefinitionQueryCondition condition);
}
