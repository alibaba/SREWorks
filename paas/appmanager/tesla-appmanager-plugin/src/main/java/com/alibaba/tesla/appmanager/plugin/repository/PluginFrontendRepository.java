package com.alibaba.tesla.appmanager.plugin.repository;

import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginFrontendQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginFrontendDO;

import java.util.List;

public interface PluginFrontendRepository {

    long countByCondition(PluginFrontendQueryCondition condition);

    int deleteByCondition(PluginFrontendQueryCondition condition);

    int insert(PluginFrontendDO record);

    List<PluginFrontendDO> selectByCondition(PluginFrontendQueryCondition condition);

    PluginFrontendDO getByCondition(PluginFrontendQueryCondition condition);

    int updateByCondition(PluginFrontendDO record, PluginFrontendQueryCondition condition);
}
