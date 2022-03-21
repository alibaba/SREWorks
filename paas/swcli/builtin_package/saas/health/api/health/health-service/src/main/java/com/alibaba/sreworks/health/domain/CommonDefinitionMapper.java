package com.alibaba.sreworks.health.domain;

import com.alibaba.sreworks.health.domain.CommonDefinition;
import com.alibaba.sreworks.health.domain.CommonDefinitionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface CommonDefinitionMapper {
    long countByExample(CommonDefinitionExample example);

    int deleteByExample(CommonDefinitionExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(CommonDefinition record);

    int insertSelective(CommonDefinition record);

    List<CommonDefinition> selectByExampleWithRowbounds(CommonDefinitionExample example, RowBounds rowBounds);

    List<CommonDefinition> selectByExample(CommonDefinitionExample example);

    CommonDefinition selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") CommonDefinition record, @Param("example") CommonDefinitionExample example);

    int updateByExample(@Param("record") CommonDefinition record, @Param("example") CommonDefinitionExample example);

    int updateByPrimaryKeySelective(CommonDefinition record);

    int updateByPrimaryKey(CommonDefinition record);

    List<CommonDefinitionGroupCount> countGroupByCategory();
}