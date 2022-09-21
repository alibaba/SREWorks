package com.alibaba.tesla.appmanager.server.repository.mapper;

import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDOExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppComponentDOMapper {
    long countByExample(AppComponentDOExample example);

    int deleteByExample(AppComponentDOExample example);

    int insertSelective(AppComponentDO record);

    List<AppComponentDO> selectByExampleWithBLOBs(AppComponentDOExample example);

    List<AppComponentDO> selectByExample(AppComponentDOExample example);

    int updateByExampleSelective(@Param("record") AppComponentDO record, @Param("example") AppComponentDOExample example);
}