package com.alibaba.tesla.appmanager.plugin.repository.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.plugin.repository.PluginTagRepository;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginTagQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginTagDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginTagDOExample;
import com.alibaba.tesla.appmanager.plugin.repository.mapper.PluginTagDOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Plugin Tag Repository
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class PluginTagRepositoryImpl implements PluginTagRepository {

    @Autowired
    private PluginTagDOMapper mapper;

    @Override
    public long countByCondition(PluginTagQueryCondition condition) {
        return mapper.countByExample(buildExample(condition));
    }

    @Override
    public int deleteByCondition(PluginTagQueryCondition condition) {
        return mapper.deleteByExample(buildExample(condition));
    }

    @Override
    public int insert(PluginTagDO record) {
        return mapper.insertSelective(insertDate(record));
    }

    @Override
    public List<PluginTagDO> selectByCondition(PluginTagQueryCondition condition) {
        return mapper.selectByExample(buildExample(condition));
    }

    @Override
    public PluginTagDO getByCondition(PluginTagQueryCondition condition) {
        List<PluginTagDO> records = selectByCondition(condition);
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
    public int updateByCondition(PluginTagDO record, PluginTagQueryCondition condition) {
        return mapper.updateByExampleSelective(updateDate(record), buildExample(condition));
    }

    private PluginTagDOExample buildExample(PluginTagQueryCondition condition) {
        PluginTagDOExample example = new PluginTagDOExample();
        PluginTagDOExample.Criteria criteria = example.createCriteria();
        if (condition.getPluginId() != null && condition.getPluginId() > 0) {
            criteria.andPluginIdEqualTo(condition.getPluginId());
        }
        return example;
    }

    private PluginTagDO insertDate(PluginTagDO record) {
        Date now = DateUtil.now();
        record.setGmtCreate(now);
        record.setGmtModified(now);
        return record;
    }

    private PluginTagDO updateDate(PluginTagDO record) {
        Date now = DateUtil.now();
        record.setGmtModified(now);
        return record;
    }
}
