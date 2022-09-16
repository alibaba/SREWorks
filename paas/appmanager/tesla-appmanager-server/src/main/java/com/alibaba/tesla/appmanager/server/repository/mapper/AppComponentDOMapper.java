package com.alibaba.tesla.appmanager.server.repository.mapper;

import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDOExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppComponentDOMapper {
    long countByExample(AppComponentDOExample example);

    int deleteByExample(AppComponentDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AppComponentDO record);

    int insertSelective(AppComponentDO record);

    List<AppComponentDO> selectByExampleWithBLOBs(AppComponentDOExample example);

    List<AppComponentDO> selectByExample(AppComponentDOExample example);

    AppComponentDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AppComponentDO record, @Param("example") AppComponentDOExample example);

    int updateByExampleWithBLOBs(@Param("record") AppComponentDO record, @Param("example") AppComponentDOExample example);

    int updateByExample(@Param("record") AppComponentDO record, @Param("example") AppComponentDOExample example);

    int updateByPrimaryKeySelective(AppComponentDO record);

    int updateByPrimaryKeyWithBLOBs(AppComponentDO record);

    int updateByPrimaryKey(AppComponentDO record);
}