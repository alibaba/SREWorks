package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.AppVersionProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.domain.dto.AppVersionDTO;
import com.alibaba.tesla.appmanager.domain.req.appversion.AppVersionCreateReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * App Version Controller
 *
 * @author jiongen.zje@alibaba-inc.com
 */
@Slf4j
@Tag(name = "应用多开发版本 API")
@RequestMapping("/apps/{appId}/versions")
@RestController
public class AppVersionController extends AppManagerBaseController {

    @Autowired
    private AppVersionProvider appVersionProvider;

    @GetMapping
    @Operation(summary = "获取应用关联的版本列表")
    public TeslaBaseResult list(
            @PathVariable String appId,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        return buildSucceedResult(appVersionProvider.list(appId));
    }

    @GetMapping("{version}")
    @Operation(summary = "获取应用版本")
    public TeslaBaseResult get(
            @PathVariable String appId,
            @PathVariable String version,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        AppVersionDTO appVersion = appVersionProvider.get(appId, version);
        return buildSucceedResult(appVersion);
    }

    @DeleteMapping("{version}")
    @Operation(summary = "删除指定应用版本")
    public TeslaBaseResult delete(
            @PathVariable String appId,
            @PathVariable String version,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        String operator = getOperator(auth);
        appVersionProvider.delete(appId, version, operator);

        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }

    @PostMapping
    @Operation(summary = "创建应用关联的版本")
    public TeslaBaseResult create(
            @PathVariable String appId,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            @RequestBody AppVersionCreateReq request,
            OAuth2Authentication auth) {

        request.setAppId(appId);
        if (request.getVersionProperties() == null){
            request.setVersionProperties(new JSONObject());
        }
        AppVersionDTO result = appVersionProvider.create(request, getOperator(auth));
        return buildSucceedResult(result);
    }



}
