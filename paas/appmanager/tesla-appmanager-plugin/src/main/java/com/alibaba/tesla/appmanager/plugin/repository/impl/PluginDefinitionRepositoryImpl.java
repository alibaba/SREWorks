package com.alibaba.tesla.appmanager.plugin.repository.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.plugin.repository.PluginDefinitionRepository;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginDefinitionQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDOExample;
import com.alibaba.tesla.appmanager.plugin.repository.mapper.PluginDefinitionDOMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Plugin Repository
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class PluginDefinitionRepositoryImpl implements PluginDefinitionRepository {

    @Autowired
    private PluginDefinitionDOMapper pluginDefinitionDOMapper;

    @Override
    public long countByCondition(PluginDefinitionQueryCondition condition) {
        return pluginDefinitionDOMapper.countByExample(buildExample(condition));
    }

    @Override
    public int deleteByCondition(PluginDefinitionQueryCondition condition) {
        return pluginDefinitionDOMapper.deleteByExample(buildExample(condition));
    }

    @Override
    public int insert(PluginDefinitionDO record) {
        return pluginDefinitionDOMapper.insertSelective(insertDate(record));
    }

    @Override
    public List<PluginDefinitionDO> selectByCondition(PluginDefinitionQueryCondition condition) {
        if (condition.isWithBlobs()) {
            return pluginDefinitionDOMapper.selectByExampleWithBLOBs(buildExample(condition));
        } else {
            return pluginDefinitionDOMapper.selectByExample(buildExample(condition));
        }
    }

    @Override
    public PluginDefinitionDO getByCondition(PluginDefinitionQueryCondition condition) {
        condition.setWithBlobs(true);
        List<PluginDefinitionDO> records = selectByCondition(condition);
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
    public int updateByCondition(PluginDefinitionDO record, PluginDefinitionQueryCondition condition) {
        return pluginDefinitionDOMapper.updateByExampleSelective(updateDate(record), buildExample(condition));
    }

    private PluginDefinitionDOExample buildExample(PluginDefinitionQueryCondition condition) {
        PluginDefinitionDOExample example = new PluginDefinitionDOExample();
        PluginDefinitionDOExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(condition.getPluginKind())) {
            criteria.andPluginKindEqualTo(condition.getPluginKind());
        }
        if (StringUtils.isNotBlank(condition.getPluginName())) {
            criteria.andPluginNameEqualTo(condition.getPluginName());
        }
        if (StringUtils.isNotBlank(condition.getPluginVersion())) {
            criteria.andPluginVersionEqualTo(condition.getPluginVersion());
        }
        return example;
    }

    private PluginDefinitionDO insertDate(PluginDefinitionDO record) {
        Date now = DateUtil.now();
        record.setGmtCreate(now);
        record.setGmtModified(now);
        return record;
    }

    private PluginDefinitionDO updateDate(PluginDefinitionDO record) {
        Date now = DateUtil.now();
        record.setGmtModified(now);
        return record;
    }
}
