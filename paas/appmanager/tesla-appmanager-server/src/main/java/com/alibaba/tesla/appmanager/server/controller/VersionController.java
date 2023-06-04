package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.AppVersionProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
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
 * 版本 Controller
 *
 * @author qianmo.zm@alibaba-inc.com
 */
@Tag(name = "版本 API")
@RequestMapping("/versions")
@RestController
@Slf4j
public class VersionController extends AppManagerBaseController {

    @Autowired
    private AppVersionProvider appVersionProvider;

    @Operation(summary = "创建全局版本")
    @PostMapping
    public TeslaBaseResult create(
            @RequestBody AppVersionCreateReq request,
            @RequestHeader(value = "X-EmpId", required = false) String empId,
            OAuth2Authentication auth) {
        String operator = getOperator(auth, empId);

        AppVersionDTO version = appVersionProvider.get("", request.getVersion());
        if(version == null) {
            AppVersionCreateReq createReq = AppVersionCreateReq.builder()
                    .appId("")
                    .version(request.getVersion())
                    .versionLabel(request.getVersionLabel())
                    .versionProperties(new JSONObject())
                    .build();
            AppVersionDTO result = appVersionProvider.create(createReq, operator);
            return buildSucceedResult(result);
        } else {
            return buildSucceedResult(version);
        }
    }
}
