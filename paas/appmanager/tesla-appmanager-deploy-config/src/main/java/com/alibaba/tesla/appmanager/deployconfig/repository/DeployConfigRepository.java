package com.alibaba.tesla.appmanager.deployconfig.repository;

import com.alibaba.tesla.appmanager.deployconfig.repository.condition.DeployConfigQueryCondition;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DeployConfigRepository {

    long countByExample(DeployConfigQueryCondition condition);

    int deleteByExample(DeployConfigQueryCondition condition);

    int insertSelective(DeployConfigDO record);

    List<DeployConfigDO> selectByExample(DeployConfigQueryCondition condition);

    int updateByExampleSelective(@Param("record") DeployConfigDO record, @Param("condition") DeployConfigQueryCondition condition);
}
