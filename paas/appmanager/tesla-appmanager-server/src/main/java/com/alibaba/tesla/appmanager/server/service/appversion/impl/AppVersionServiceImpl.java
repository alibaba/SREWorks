package com.alibaba.tesla.appmanager.server.service.appversion.impl;

import com.alibaba.tesla.appmanager.server.repository.AppVersionRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppVersionQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppVersionDO;
import com.alibaba.tesla.appmanager.server.service.appversion.AppVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AppVersionServiceImpl implements AppVersionService {

    @Autowired
    private AppVersionRepository appVersionRepository;

    @Override
    public AppVersionDO get(AppVersionQueryCondition condition) {
        condition.setWithBlobs(true);
        List<AppVersionDO> records = appVersionRepository.selectByCondition(condition);
        if (records.size() == 0) {
            return null;
        } else {
            return records.get(0);
        }
    }

    @Override
    public List<AppVersionDO> gets(AppVersionQueryCondition condition) {
        return  appVersionRepository.selectByCondition(condition);
    }

    @Override
    public int create(AppVersionDO record) {
        return appVersionRepository.insert(record);
    }

    @Override
    public int delete(AppVersionQueryCondition condition) {
        return appVersionRepository.deleteByCondition(condition);
    }
}
