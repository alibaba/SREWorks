package com.alibaba.tesla.appmanager.server.service.appenvironment;

import com.alibaba.tesla.appmanager.domain.container.AppComponentLocationContainer;
import com.alibaba.tesla.appmanager.domain.req.appenvironment.AppEnvironmentBindReq;

import java.util.List;

/**
 * 应用环境服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface AppEnvironmentService {

    /**
     * 将指定应用加入到指定环境中
     *
     * @param req 加入环境请求
     */
    void bindEnvironment(AppEnvironmentBindReq req);
}
