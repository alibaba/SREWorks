package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.AppComponentProvider;
import com.alibaba.tesla.appmanager.api.provider.AppEnvironmentProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.domain.container.BizAppContainer;
import com.alibaba.tesla.appmanager.domain.req.appenvironment.AppEnvironmentBindReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * App Environment Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Tag(name = "应用环境关联 API")
@RequestMapping("/apps/{appId}/environments")
@RestController
public class AppEnvironmentController extends AppManagerBaseController {

    @Autowired
    private AppEnvironmentProvider appEnvironmentProvider;

    @Autowired
    private AppComponentProvider appComponentProvider;

    @Operation(summary = "将指定应用加入到指定环境中")
    @PutMapping("bind")
    public TeslaBaseResult bind(
            @RequestBody AppEnvironmentBindReq request,
            @PathVariable String appId,
            @RequestHeader(value = "X-Biz-App") String headerBizApp,
            OAuth2Authentication auth) {
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setIsolateNamespaceId(container.getNamespaceId());
        request.setIsolateStageId(container.getStageId());
        request.setAppId(appId);
        request.setApiVersion(DefaultConstant.API_VERSION_V1_ALPHA2);
        request.setOperator(getOperator(auth));
        request.setAppComponents(appComponentProvider.getFullComponentRelations(
                appId, container.getNamespaceId(), container.getStageId()));
        appEnvironmentProvider.bindEnvironment(request);
        return buildSucceedResult();
    }
}