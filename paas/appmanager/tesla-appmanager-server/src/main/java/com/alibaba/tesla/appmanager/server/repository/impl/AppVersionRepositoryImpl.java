package com.alibaba.tesla.appmanager.server.repository.impl;

import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.server.repository.AppVersionRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppVersionQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppVersionDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppVersionDOExample;
import com.alibaba.tesla.appmanager.server.repository.mapper.AppVersionDOMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AppVersionRepositoryImpl implements AppVersionRepository {

    @Autowired
    private AppVersionDOMapper appVersionDOMapper;

    @Override
    public long countByCondition(AppVersionQueryCondition condition) {
        return appVersionDOMapper.countByExample(buildExample(condition));
    }

    @Override
    public int deleteByCondition(AppVersionQueryCondition condition) {
        return appVersionDOMapper.deleteByExample(buildExample(condition));
    }

    @Override
    public int insert(AppVersionDO record) {
        return appVersionDOMapper.insertSelective(insertDate(record));
    }

    @Override
    public List<AppVersionDO> selectByCondition(AppVersionQueryCondition condition) {
        AppVersionDOExample example = buildExample(condition);
        if (condition.isWithBlobs()) {
            return appVersionDOMapper.selectByExampleWithBLOBs(example);
        } else {
            return appVersionDOMapper.selectByExample(example);
        }
    }

    @Override
    public int updateByCondition(AppVersionDO record, AppVersionQueryCondition condition) {
        return appVersionDOMapper.updateByExampleSelective(updateDate(record), buildExample(condition));
    }

    private AppVersionDOExample buildExample(AppVersionQueryCondition condition) {
        AppVersionDOExample example = new AppVersionDOExample();
        AppVersionDOExample.Criteria criteria = example.createCriteria();
        if (condition.getAppId() != null) {
            criteria.andAppIdEqualTo(condition.getAppId());
        }
        if (StringUtils.isNotEmpty(condition.getVersion())) {
            criteria.andVersionEqualTo(condition.getVersion());
        }
        if (StringUtils.isNotEmpty(condition.getVersionLabel())) {
            criteria.andVersionLabelEqualTo(condition.getVersionLabel());
        }
        return example;
    }

    private AppVersionDO insertDate(AppVersionDO record) {
        Date now = DateUtil.now();
        record.setGmtCreate(now);
        record.setGmtModified(now);
        return record;
    }

    private AppVersionDO updateDate(AppVersionDO record) {
        Date now = DateUtil.now();
        record.setGmtModified(now);
        return record;
    }
}
