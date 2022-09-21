package com.alibaba.tesla.appmanager.server.repository;

import com.alibaba.tesla.appmanager.server.repository.condition.AppComponentQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDO;

import java.util.List;

public interface AppComponentRepository {

    long countByCondition(AppComponentQueryCondition condition);

    int deleteByCondition(AppComponentQueryCondition condition);

    int insert(AppComponentDO record);

    List<AppComponentDO> selectByCondition(AppComponentQueryCondition condition);

    int updateByCondition(AppComponentDO record, AppComponentQueryCondition condition);
}
