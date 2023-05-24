package com.alibaba.tesla.appmanager.server.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.AppComponentProvider;
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

    @Autowired
    private AppComponentProvider appComponentProvider;

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
        return appVersionDtoConvert.to(appVersionService.getsAll(appId));
    }

    @Override
    public void delete(String appId, String version, String operator) {
        AppVersionQueryCondition condition = new AppVersionQueryCondition();
        condition.setAppId(appId);
        condition.setVersion(version);
        AppVersionDO appVersion = appVersionService.get(condition);
        log.info("try to remove app version {} {} {}", appId, version, appVersion);
        if(appVersion != null) {
            appVersionService.delete(condition);
        }
        String[] env = version.split(",");
        appComponentProvider.clean(appId, env[0], env[1], operator);
    }


    @Override
    public void clean(String appId, String operator) {
        List<AppVersionDO> versions = appVersionService.getsAll(appId);
        for(AppVersionDO version : versions) {
            delete(appId, version.getVersion(), operator);
        }
    }
}
