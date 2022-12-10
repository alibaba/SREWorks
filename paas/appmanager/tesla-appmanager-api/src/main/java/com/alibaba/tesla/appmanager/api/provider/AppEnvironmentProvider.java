package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.domain.req.appenvironment.AppEnvironmentBindReq;

/**
 * 应用环境关联 Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface AppEnvironmentProvider {

    /**
     * 将指定应用加入到指定环境中
     *
     * @param req 加入环境请求
     */
    void bindEnvironment(AppEnvironmentBindReq req);
}
