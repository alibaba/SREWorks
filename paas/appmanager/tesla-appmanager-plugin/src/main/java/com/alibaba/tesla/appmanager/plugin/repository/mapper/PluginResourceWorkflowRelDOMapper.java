package com.alibaba.tesla.appmanager.plugin.repository.mapper;

import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceWorkflowRelDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceWorkflowRelDOExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PluginResourceWorkflowRelDOMapper {
    long countByExample(PluginResourceWorkflowRelDOExample example);

    int deleteByExample(PluginResourceWorkflowRelDOExample example);

    int insertSelective(PluginResourceWorkflowRelDO record);

    List<PluginResourceWorkflowRelDO> selectByExample(PluginResourceWorkflowRelDOExample example);

    int updateByExampleSelective(@Param("record") PluginResourceWorkflowRelDO record, @Param("example") PluginResourceWorkflowRelDOExample example);
}