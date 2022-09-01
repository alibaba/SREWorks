package com.alibaba.tesla.appmanager.plugin.repository.mapper;

import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDOExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PluginDefinitionDOMapper {
    long countByExample(PluginDefinitionDOExample example);

    int deleteByExample(PluginDefinitionDOExample example);

    int insertSelective(PluginDefinitionDO record);

    List<PluginDefinitionDO> selectByExampleWithBLOBs(PluginDefinitionDOExample example);

    List<PluginDefinitionDO> selectByExample(PluginDefinitionDOExample example);

    int updateByExampleSelective(@Param("record") PluginDefinitionDO record, @Param("example") PluginDefinitionDOExample example);
}