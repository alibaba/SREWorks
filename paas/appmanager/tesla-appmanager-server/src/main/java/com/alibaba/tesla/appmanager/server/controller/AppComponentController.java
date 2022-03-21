package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.AppComponentProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentQueryReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Component Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@RequestMapping("/apps/{appId}/components")
@RestController
public class AppComponentController extends AppManagerBaseController {

    @Autowired
    private AppComponentProvider appComponentProvider;

    /**
     * @api {get} /apps/:appId/components 获取应用绑定的组件列表
     * @apiName GetApplicationComponentList
     * @apiGroup 应用关联组件 API
     * @apiParam (Path Parameters) {String} appId 应用 ID
     */
    @GetMapping
    public TeslaBaseResult list(
            @PathVariable String appId, OAuth2Authentication auth) {
        AppComponentQueryReq request = AppComponentQueryReq.builder().appId(appId).build();
        return buildSucceedResult(appComponentProvider.list(request, getOperator(auth)));
    }
}
