package com.alibaba.tesla.appmanager.server.repository.impl;

import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.server.repository.AppComponentRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppComponentQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDOExample;
import com.alibaba.tesla.appmanager.server.repository.mapper.AppComponentDOMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 应用组件绑定 Repository
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class AppComponentRepositoryImpl implements AppComponentRepository {

    @Autowired
    private AppComponentDOMapper mapper;

    @Override
    public long countByCondition(AppComponentQueryCondition condition) {
        return mapper.countByExample(buildExample(condition));
    }

    @Override
    public int deleteByCondition(AppComponentQueryCondition condition) {
        return mapper.deleteByExample(buildExample(condition));
    }

    @Override
    public int insert(AppComponentDO record) {
        return mapper.insertSelective(insertDate(record));
    }

    @Override
    public List<AppComponentDO> selectByCondition(AppComponentQueryCondition condition) {
        AppComponentDOExample example = buildExample(condition);
        if (condition.isWithBlobs()) {
            return mapper.selectByExampleWithBLOBs(example);
        } else {
            return mapper.selectByExample(example);
        }
    }

    @Override
    public int updateByCondition(AppComponentDO record, AppComponentQueryCondition condition) {
        return mapper.updateByExampleSelective(updateDate(record), buildExample(condition));
    }

    private AppComponentDOExample buildExample(AppComponentQueryCondition condition) {
        AppComponentDOExample example = new AppComponentDOExample();
        AppComponentDOExample.Criteria criteria = example.createCriteria();
        if (condition.getId() != null && condition.getId() > 0) {
            criteria.andIdEqualTo(condition.getId());
        }
        if (StringUtils.isNotBlank(condition.getAppId())) {
            criteria.andAppIdEqualTo(condition.getAppId());
        }
        if (StringUtils.isNotBlank(condition.getCategory())) {
            criteria.andCategoryEqualTo(condition.getCategory());
        }
        if (StringUtils.isNotBlank(condition.getComponentType())) {
            criteria.andComponentTypeEqualTo(condition.getComponentType());
        }
        if (StringUtils.isNotBlank(condition.getComponentName())) {
            criteria.andComponentNameEqualTo(condition.getComponentName());
        }
        if (StringUtils.isNotBlank(condition.getNamespaceId())) {
            criteria.andNamespaceIdEqualTo(condition.getNamespaceId());
        }
        if (StringUtils.isNotBlank(condition.getStageId())) {
            criteria.andStageIdEqualTo(condition.getStageId());
        }
        return example;
    }

    private AppComponentDO insertDate(AppComponentDO record) {
        Date now = DateUtil.now();
        record.setGmtCreate(now);
        record.setGmtModified(now);
        return record;
    }

    private AppComponentDO updateDate(AppComponentDO record) {
        Date now = DateUtil.now();
        record.setGmtModified(now);
        return record;
    }
}
