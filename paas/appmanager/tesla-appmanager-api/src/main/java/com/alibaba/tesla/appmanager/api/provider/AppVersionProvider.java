package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.domain.dto.AppVersionDTO;
import com.alibaba.tesla.appmanager.domain.req.appversion.AppVersionCreateReq;

import java.util.List;

/**
 * 应用关联版本 Provider
 *
 * @author jiongen.zje@alibaba-inc.com
 */
public interface AppVersionProvider {

    AppVersionDTO get(String appId, String version);

    /**
     * 创建应用版本
     *
     * @param request  创建请求
     * @param operator 操作人
     * @return 版本信息
     */
    AppVersionDTO create(AppVersionCreateReq request, String operator);


    List<AppVersionDTO> list(String appId);

    void delete(String appId, String version, String operator);

    void clean(String appId, String operator);

}
