package com.alibaba.tesla.appmanager.server.provider.impl;

import com.alibaba.tesla.appmanager.api.provider.AppEnvironmentProvider;
import com.alibaba.tesla.appmanager.domain.req.appenvironment.AppEnvironmentBindReq;
import com.alibaba.tesla.appmanager.server.service.appenvironment.AppEnvironmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 应用环境关联 Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Service
public class AppEnvironmentProviderImpl implements AppEnvironmentProvider {

    @Autowired
    private AppEnvironmentService appEnvironmentService;

    /**
     * 将指定应用加入到指定环境中
     *
     * @param req 加入环境请求
     */
    @Override
    public void bindEnvironment(AppEnvironmentBindReq req) {
        appEnvironmentService.bindEnvironment(req);
    }
}
