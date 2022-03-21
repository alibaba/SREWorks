package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.DeployConfigProvider;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigApplyTemplateReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigDeleteReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigGenerateReq;
import com.alibaba.tesla.appmanager.domain.res.apppackage.ApplicationConfigurationGenerateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigGenerateRes;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Application Configuration Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@RequestMapping("/application-configurations")
@RestController
@Slf4j
public class ApplicationConfigurationController extends BaseController {

    @Autowired
    private DeployConfigProvider deployConfigProvider;

    /**
     * @api {put} /application-configurations 更新全局部署信息
     * @apiName PutApplicationConfigurations
     * @apiGroup Application Configuration API
     */
    @PutMapping
    public TeslaBaseResult update(@RequestBody DeployConfigApplyTemplateReq request) {
        if (StringUtils.isEmpty(request.getAppId())) {
            request.setAppId("");
        }
        return buildSucceedResult(deployConfigProvider.applyTemplate(request));
    }

    /**
     * @api {get} /application-configurations 获取全局部署信息
     * @apiName GetApplicationConfigurations
     * @apiGroup Application Configuration API
     */
    @GetMapping
    public TeslaBaseResult get(@ModelAttribute DeployConfigGenerateReq request) {
        if (StringUtils.isEmpty(request.getApiVersion())) {
            request.setApiVersion(DefaultConstant.API_VERSION_V1_ALPHA2);
        }
        if (StringUtils.isEmpty(request.getAppId())) {
            request.setAppId("");
        }
        request.setAppPackageId(0L);
        DeployConfigGenerateRes result = deployConfigProvider.generate(request);
        return buildSucceedResult(ApplicationConfigurationGenerateRes.builder()
                .yaml(SchemaUtil.toYamlMapStr(result.getSchema()))
                .build());
    }

    /**
     * @api {delete} /application-configurations 获取全局部署信息
     * @apiName GetApplicationConfigurations
     * @apiGroup Application Configuration API
     */
    @DeleteMapping
    public TeslaBaseResult delete(@ModelAttribute DeployConfigDeleteReq request) {
        if (StringUtils.isEmpty(request.getApiVersion())) {
            request.setApiVersion(DefaultConstant.API_VERSION_V1_ALPHA2);
        }
        if (StringUtils.isEmpty(request.getAppId())) {
            request.setAppId("");
        }
        deployConfigProvider.delete(request);
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }
}
