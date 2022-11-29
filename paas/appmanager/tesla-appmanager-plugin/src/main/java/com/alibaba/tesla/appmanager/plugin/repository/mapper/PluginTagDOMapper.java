package com.alibaba.tesla.appmanager.plugin.repository.mapper;

import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginTagDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginTagDOExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PluginTagDOMapper {
    long countByExample(PluginTagDOExample example);

    int deleteByExample(PluginTagDOExample example);

    int insertSelective(PluginTagDO record);

    List<PluginTagDO> selectByExample(PluginTagDOExample example);

    int updateByExampleSelective(@Param("record") PluginTagDO record, @Param("example") PluginTagDOExample example);
}