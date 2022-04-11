package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.domain.req.groovy.GroovyUpgradeReq;
import com.alibaba.tesla.appmanager.server.service.groovy.GroovyService;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Groovy Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@RequestMapping("/groovy")
@RestController
public class GroovyController extends AppManagerBaseController {

    @Autowired
    private GroovyService groovyService;

    // 升级 Groovy 脚本
    @PostMapping
    @ResponseBody
    public TeslaBaseResult upgradeScript(
            @RequestBody GroovyUpgradeReq req, OAuth2Authentication auth) throws Exception {
        groovyService.upgradeScript(req);
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }
}
