package com.alibaba.tesla.appmanager.server.service.appcomponent.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.server.repository.AppComponentRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppComponentQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDO;
import com.alibaba.tesla.appmanager.server.service.appcomponent.AppComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 应用绑定组件服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class AppComponentServiceImpl implements AppComponentService {

    @Autowired
    private AppComponentRepository appComponentRepository;

    /**
     * 根据条件过滤应用绑定组件
     *
     * @param condition 过滤条件
     * @return List
     */
    @Override
    public List<AppComponentDO> list(AppComponentQueryCondition condition) {
        return appComponentRepository.selectByCondition(condition);
    }

    /**
     * 根据条件获取指定的应用绑定组件
     *
     * @param condition 查询条件
     * @return List
     */
    @Override
    public AppComponentDO get(AppComponentQueryCondition condition) {
        condition.setWithBlobs(true);
        List<AppComponentDO> records = appComponentRepository.selectByCondition(condition);
        if (records.size() == 0) {
            return null;
        } else {
            return records.get(0);
        }
    }

    /**
     * 创建应用绑定组件
     *
     * @param record 绑定记录
     * @return 数据库更新数量
     */
    @Override
    public int create(AppComponentDO record) {
        return appComponentRepository.insert(record);
    }

    /**
     * 更新应用绑定组件
     *
     * @param record    绑定记录
     * @param condition 查询条件
     * @return 数据库更新数量
     */
    @Override
    public int update(AppComponentDO record, AppComponentQueryCondition condition) {
        return appComponentRepository.updateByCondition(record, condition);
    }

    /**
     * 删除应用绑定组件
     *
     * @param condition 查询条件
     * @return 数据库更新数量
     */
    @Override
    public int delete(AppComponentQueryCondition condition) {
        return appComponentRepository.deleteByCondition(condition);
    }
}
