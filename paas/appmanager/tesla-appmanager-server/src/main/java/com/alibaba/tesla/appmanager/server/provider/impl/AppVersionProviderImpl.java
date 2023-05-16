package com.alibaba.tesla.appmanager.server.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.AppVersionProvider;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.dto.AppVersionDTO;
import com.alibaba.tesla.appmanager.domain.req.appversion.AppVersionCreateReq;
import com.alibaba.tesla.appmanager.server.assembly.AppVersionDtoConvert;
import com.alibaba.tesla.appmanager.server.repository.condition.AppVersionQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppVersionDO;
import com.alibaba.tesla.appmanager.server.service.appversion.AppVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 应用关联组件 Provider
 *
 * @author jiongen.zje@alibaba-inc.com
 */
@Service
@Slf4j
public class AppVersionProviderImpl implements AppVersionProvider {

    @Autowired
    private AppVersionDtoConvert appVersionDtoConvert;

    @Autowired
    private AppVersionService appVersionService;

    @Override
    public AppVersionDTO get(String appId, String version) {
        AppVersionQueryCondition condition = new AppVersionQueryCondition();
        condition.setAppId(appId);
        condition.setVersion(version);
        return appVersionDtoConvert.to(appVersionService.get(condition));
    }

    @Override
    public AppVersionDTO create(AppVersionCreateReq request, String operator) {
        AppVersionDO record = AppVersionDO.builder()
                .appId(request.getAppId())
                .version(request.getVersion())
                .versionLabel(request.getVersionLabel())
                .versionProperties(request.getVersionProperties().toJSONString())
                .build();
        int count = appVersionService.create(record);
        if (count != 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid count when creating app version|count=%d|record=%s",
                            count, JSONObject.toJSONString(record)));
        }

        return appVersionDtoConvert.to(appVersionService.get(AppVersionQueryCondition.builder()
                .appId(request.getAppId())
                .version(request.getVersion())
                .build()));
    }

    @Override
    public List<AppVersionDTO> list(String appId) {
        AppVersionQueryCondition condition = new AppVersionQueryCondition();
        condition.setAppId(appId);
        return appVersionDtoConvert.to(appVersionService.gets(condition));
    }

}
