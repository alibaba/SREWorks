package com.alibaba.tesla.appmanager.plugin.repository.mapper;

import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceDOExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PluginResourceDOMapper {
    long countByExample(PluginResourceDOExample example);

    int deleteByExample(PluginResourceDOExample example);

    int insertSelective(PluginResourceDO record);

    List<PluginResourceDO> selectByExample(PluginResourceDOExample example);

    int updateByExampleSelective(@Param("record") PluginResourceDO record, @Param("example") PluginResourceDOExample example);
}