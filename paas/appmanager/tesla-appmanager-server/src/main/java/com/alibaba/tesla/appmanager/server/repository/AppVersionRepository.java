package com.alibaba.tesla.appmanager.server.repository;

import com.alibaba.tesla.appmanager.server.repository.condition.AppVersionQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppVersionDO;

import java.util.List;

public interface AppVersionRepository {
    long countByCondition(AppVersionQueryCondition condition);

    int deleteByCondition(AppVersionQueryCondition condition);

    int insert(AppVersionDO record);

    List<AppVersionDO> selectByCondition(AppVersionQueryCondition condition);

    int updateByCondition(AppVersionDO record, AppVersionQueryCondition condition);
}