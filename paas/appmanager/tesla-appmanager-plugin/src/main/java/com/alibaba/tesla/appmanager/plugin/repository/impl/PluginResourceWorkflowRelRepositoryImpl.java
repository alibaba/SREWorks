package com.alibaba.tesla.appmanager.plugin.repository.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.plugin.repository.PluginResourceWorkflowRelRepository;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginResourceWorkflowRelQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceWorkflowRelDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginResourceWorkflowRelDOExample;
import com.alibaba.tesla.appmanager.plugin.repository.mapper.PluginResourceWorkflowRelDOMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Plugin Workflow Rel Repository
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class PluginResourceWorkflowRelRepositoryImpl implements PluginResourceWorkflowRelRepository {

    @Autowired
    private PluginResourceWorkflowRelDOMapper mapper;

    @Override
    public long countByCondition(PluginResourceWorkflowRelQueryCondition condition) {
        return mapper.countByExample(buildExample(condition));
    }

    @Override
    public int deleteByCondition(PluginResourceWorkflowRelQueryCondition condition) {
        return mapper.deleteByExample(buildExample(condition));
    }

    @Override
    public int insert(PluginResourceWorkflowRelDO record) {
        return mapper.insertSelective(insertDate(record));
    }

    @Override
    public List<PluginResourceWorkflowRelDO> selectByCondition(PluginResourceWorkflowRelQueryCondition condition) {
        return mapper.selectByExample(buildExample(condition));
    }

    @Override
    public PluginResourceWorkflowRelDO getByCondition(PluginResourceWorkflowRelQueryCondition condition) {
        List<PluginResourceWorkflowRelDO> records = selectByCondition(condition);
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
    public int updateByCondition(PluginResourceWorkflowRelDO record, PluginResourceWorkflowRelQueryCondition condition) {
        return mapper.updateByExampleSelective(updateDate(record), buildExample(condition));
    }

    private PluginResourceWorkflowRelDOExample buildExample(PluginResourceWorkflowRelQueryCondition condition) {
        PluginResourceWorkflowRelDOExample example = new PluginResourceWorkflowRelDOExample();
        PluginResourceWorkflowRelDOExample.Criteria criteria = example.createCriteria();
        if (condition.getPluginResourceId() != null && condition.getPluginResourceId() > 0) {
            criteria.andPluginResourceIdEqualTo(condition.getPluginResourceId());
        }
        if (StringUtils.isNotBlank(condition.getClusterId())) {
            criteria.andClusterIdEqualTo(condition.getClusterId());
        }
        if (StringUtils.isNotBlank(condition.getWorkflowType())) {
            criteria.andWorkflowTypeEqualTo(condition.getWorkflowType());
        }
        return example;
    }

    private PluginResourceWorkflowRelDO insertDate(PluginResourceWorkflowRelDO record) {
        Date now = DateUtil.now();
        record.setGmtCreate(now);
        record.setGmtModified(now);
        return record;
    }

    private PluginResourceWorkflowRelDO updateDate(PluginResourceWorkflowRelDO record) {
        Date now = DateUtil.now();
        record.setGmtModified(now);
        return record;
    }
}
