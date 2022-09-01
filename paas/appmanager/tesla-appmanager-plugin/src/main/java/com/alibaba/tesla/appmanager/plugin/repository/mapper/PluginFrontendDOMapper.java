package com.alibaba.tesla.appmanager.plugin.repository.mapper;

import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginFrontendDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginFrontendDOExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PluginFrontendDOMapper {
    long countByExample(PluginFrontendDOExample example);

    int deleteByExample(PluginFrontendDOExample example);

    int insertSelective(PluginFrontendDO record);

    List<PluginFrontendDO> selectByExample(PluginFrontendDOExample example);

    int updateByExampleSelective(@Param("record") PluginFrontendDO record, @Param("example") PluginFrontendDOExample example);
}