package com.alibaba.tesla.appmanager.plugin.repository.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.plugin.repository.PluginResourceRepository;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginResourceQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceDOExample;
import com.alibaba.tesla.appmanager.plugin.repository.mapper.PluginResourceDOMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Plugin Resource Repository
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class PluginResourceRepositoryImpl implements PluginResourceRepository {

    @Autowired
    private PluginResourceDOMapper mapper;

    @Override
    public long countByCondition(PluginResourceQueryCondition condition) {
        return mapper.countByExample(buildExample(condition));
    }

    @Override
    public int deleteByCondition(PluginResourceQueryCondition condition) {
        return mapper.deleteByExample(buildExample(condition));
    }

    @Override
    public int insert(PluginResourceDO record) {
        return mapper.insertSelective(insertDate(record));
    }

    @Override
    public List<PluginResourceDO> selectByCondition(PluginResourceQueryCondition condition) {
        return mapper.selectByExample(buildExample(condition));
    }

    @Override
    public PluginResourceDO getByCondition(PluginResourceQueryCondition condition) {
        List<PluginResourceDO> records = selectByCondition(condition);
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
    public int updateByCondition(PluginResourceDO record, PluginResourceQueryCondition condition) {
        return mapper.updateByExampleSelective(updateDate(record), buildExample(condition));
    }

    private PluginResourceDOExample buildExample(PluginResourceQueryCondition condition) {
        PluginResourceDOExample example = new PluginResourceDOExample();
        PluginResourceDOExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(condition.getPluginName())) {
            criteria.andPluginNameEqualTo(condition.getPluginName());
        }
        if (StringUtils.isNotBlank(condition.getPluginVersion())) {
            criteria.andPluginVersionEqualTo(condition.getPluginVersion());
        }
        if (StringUtils.isNotBlank(condition.getClusterId())) {
            criteria.andClusterIdEqualTo(condition.getClusterId());
        }
        if (StringUtils.isNotBlank(condition.getInstanceStatus())) {
            criteria.andInstanceStatusEqualTo(condition.getInstanceStatus());
        }
        if (condition.getInstanceRegistered() != null) {
            criteria.andInstanceRegisteredEqualTo(condition.getInstanceRegistered());
        }
        return example;
    }

    private PluginResourceDO insertDate(PluginResourceDO record) {
        Date now = DateUtil.now();
        record.setGmtCreate(now);
        record.setGmtModified(now);
        return record;
    }

    private PluginResourceDO updateDate(PluginResourceDO record) {
        Date now = DateUtil.now();
        record.setGmtModified(now);
        return record;
    }
}
