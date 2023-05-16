package com.alibaba.tesla.appmanager.server.service.appversion;

import com.alibaba.tesla.appmanager.server.repository.condition.AppVersionQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppVersionDO;

import java.util.List;

public interface AppVersionService {

    AppVersionDO get(AppVersionQueryCondition condition);

    List<AppVersionDO> gets(AppVersionQueryCondition condition);

    int create(AppVersionDO record);

    int delete(AppVersionQueryCondition condition);
}
