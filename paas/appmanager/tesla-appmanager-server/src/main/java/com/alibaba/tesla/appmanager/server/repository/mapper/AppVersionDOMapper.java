package com.alibaba.tesla.appmanager.server.repository.mapper;

import com.alibaba.tesla.appmanager.server.repository.domain.AppVersionDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppVersionDOExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppVersionDOMapper {
    long countByExample(AppVersionDOExample example);

    int deleteByExample(AppVersionDOExample example);

    int insertSelective(AppVersionDO record);

    List<AppVersionDO> selectByExampleWithBLOBs(AppVersionDOExample example);

    List<AppVersionDO> selectByExample(AppVersionDOExample example);

    int updateByExampleSelective(@Param("record") AppVersionDO record, @Param("example") AppVersionDOExample example);
}