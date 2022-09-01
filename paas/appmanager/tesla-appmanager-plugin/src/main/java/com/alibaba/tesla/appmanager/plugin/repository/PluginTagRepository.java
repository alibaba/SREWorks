package com.alibaba.tesla.appmanager.plugin.repository;

import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginTagQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginTagDO;

import java.util.List;

public interface PluginTagRepository {

    long countByCondition(PluginTagQueryCondition condition);

    int deleteByCondition(PluginTagQueryCondition condition);

    int insert(PluginTagDO record);

    List<PluginTagDO> selectByCondition(PluginTagQueryCondition condition);

    PluginTagDO getByCondition(PluginTagQueryCondition condition);

    int updateByCondition(PluginTagDO record, PluginTagQueryCondition condition);
}
