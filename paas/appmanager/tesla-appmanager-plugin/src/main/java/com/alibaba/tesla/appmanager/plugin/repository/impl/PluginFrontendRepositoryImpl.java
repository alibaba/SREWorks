package com.alibaba.tesla.appmanager.plugin.repository.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.plugin.repository.PluginFrontendRepository;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginFrontendQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginFrontendDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginFrontendDOExample;
import com.alibaba.tesla.appmanager.plugin.repository.mapper.PluginFrontendDOMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Plugin Frontend Repository
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class PluginFrontendRepositoryImpl implements PluginFrontendRepository {

    @Autowired
    private PluginFrontendDOMapper mapper;

    @Override
    public long countByCondition(PluginFrontendQueryCondition condition) {
        return mapper.countByExample(buildExample(condition));
    }

    @Override
    public int deleteByCondition(PluginFrontendQueryCondition condition) {
        return mapper.deleteByExample(buildExample(condition));
    }

    @Override
    public int insert(PluginFrontendDO record) {
        return mapper.insertSelective(insertDate(record));
    }

    @Override
    public List<PluginFrontendDO> selectByCondition(PluginFrontendQueryCondition condition) {
        return mapper.selectByExample(buildExample(condition));
    }

    @Override
    public PluginFrontendDO getByCondition(PluginFrontendQueryCondition condition) {
        List<PluginFrontendDO> records = selectByCondition(condition);
        if (records.size() == 0) {
            return null;
        } else if (records.size() > 1) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                    "multiple plugin definition found|%s", JSONObject.toJSONString(condition));
        } else {
            return records.get(0);
        }
    }

    @Override
    public int updateByCondition(PluginFrontendDO record, PluginFrontendQueryCondition condition) {
        return mapper.updateByExampleSelective(updateDate(record), buildExample(condition));
    }

    private PluginFrontendDOExample buildExample(PluginFrontendQueryCondition condition) {
        PluginFrontendDOExample example = new PluginFrontendDOExample();
        PluginFrontendDOExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(condition.getPluginName())) {
            criteria.andPluginNameEqualTo(condition.getPluginName());
        }
        if (StringUtils.isNotBlank(condition.getPluginVersion())) {
            criteria.andPluginVersionEqualTo(condition.getPluginVersion());
        }
        if (StringUtils.isNotBlank(condition.getName())) {
            criteria.andNameEqualTo(condition.getName());
        }
        return example;
    }

    private PluginFrontendDO insertDate(PluginFrontendDO record) {
        Date now = DateUtil.now();
        record.setGmtCreate(now);
        record.setGmtModified(now);
        return record;
    }

    private PluginFrontendDO updateDate(PluginFrontendDO record) {
        Date now = DateUtil.now();
        record.setGmtModified(now);
        return record;
    }
}
