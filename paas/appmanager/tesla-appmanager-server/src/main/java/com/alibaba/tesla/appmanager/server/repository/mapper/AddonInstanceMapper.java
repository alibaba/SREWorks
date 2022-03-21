package com.alibaba.tesla.appmanager.server.repository.mapper;

import com.alibaba.tesla.appmanager.server.repository.domain.AddonInstanceDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AddonInstanceDOExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Mybatis Generator 2020/09/29
 */
public interface AddonInstanceMapper {
    long countByExample(AddonInstanceDOExample example);

    int deleteByExample(AddonInstanceDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AddonInstanceDO record);

    int insertOrUpdate(AddonInstanceDO record);

    int insertOrUpdateSelective(AddonInstanceDO record);

    int insertSelective(AddonInstanceDO record);

    List<AddonInstanceDO> selectByExample(AddonInstanceDOExample example);

    AddonInstanceDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AddonInstanceDO record, @Param("example") AddonInstanceDOExample example);

    int updateByExample(@Param("record") AddonInstanceDO record, @Param("example") AddonInstanceDOExample example);

    int updateByPrimaryKeySelective(AddonInstanceDO record);

    int updateByPrimaryKey(AddonInstanceDO record);
}