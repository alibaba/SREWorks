package com.alibaba.sreworks.appdev.server.controllers;

import java.io.IOException;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.appdev.server.params.AppDeployStartParam;
import com.alibaba.sreworks.appdev.server.params.AppDeployUpdateParam;
import com.alibaba.sreworks.appdev.server.services.AppInstallService;
import com.alibaba.sreworks.appdev.server.services.AppUninstallService;
import com.alibaba.sreworks.appdev.server.services.AppUpdateService;
import com.alibaba.sreworks.common.util.StringUtil;
import com.alibaba.sreworks.domain.DO.AppComponent;
import com.alibaba.sreworks.domain.DO.AppComponentInstance;
import com.alibaba.sreworks.domain.DO.AppInstance;
import com.alibaba.sreworks.domain.DO.AppPackage;
import com.alibaba.sreworks.domain.repository.AppComponentInstanceRepository;
import com.alibaba.sreworks.domain.repository.AppComponentRepository;
import com.alibaba.sreworks.domain.repository.AppInstanceRepository;
import com.alibaba.sreworks.domain.repository.AppPackageRepository;
import com.alibaba.sreworks.domain.repository.AppRepository;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAppmanagerComponentService;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAppmanagerDeployService;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;

import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jinghua.yjh
 */
@Slf4j
@RestController
@RequestMapping("/appdev/appDeploy")
@Api(tags = "应用部署")
public class AppDeployController extends BaseController {

    @Autowired
    FlyadminAppmanagerDeployService flyadminAppmanagerDeployService;

    @Autowired
    AppInstanceRepository appInstanceRepository;

    @Autowired
    AppComponentRepository appComponentRepository;

    @Autowired
    AppComponentInstanceRepository appComponentInstanceRepository;

    @Autowired
    AppInstallService appInstallService;

    @Autowired
    AppUninstallService appUninstallService;

    @Autowired
    AppRepository appRepository;

    @Autowired
    FlyadminAppmanagerComponentService flyadminAppmanagerComponentService;

    @Autowired
    AppUpdateService appUpdateService;

    @Autowired
    AppPackageRepository appPackageRepository;

    @ApiOperation(value = "列表")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list(Long appId, String user) throws IOException, ApiException {
        user = StringUtil.isEmpty(user) ? getUserEmployeeId() : user;
        List<JSONObject> ret = flyadminAppmanagerDeployService.list(appId, user);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "部署")
    @RequestMapping(value = "deploy", method = RequestMethod.POST)
    public TeslaBaseResult deploy(Long appPackageId, @RequestBody AppDeployStartParam param)
        throws IOException, ApiException {

        if (appPackageId != null) {
            param.setAppPackageId(appPackageId);
        }
        AppPackage appPackage = appPackageRepository.findFirstById(param.getAppPackageId());
        if (param.getTeamId() == null) {
            param.setTeamId(appPackage.app().getTeamId());
        }

        // appInstance 创建
        AppInstance appInstance = param.toAppInstance(appPackage, getUserEmployeeId());
        appInstanceRepository.saveAndFlush(appInstance);

        // appComponentInstanceList 创建
        List<AppComponent> appComponentList = appPackage.appComponentList();
        List<AppComponentInstance> appComponentInstanceList = param.toAppComponentInstanceList(
            appInstance, appComponentList, getUserEmployeeId());
        appComponentInstanceRepository.saveAll(appComponentInstanceList);
        appComponentInstanceRepository.flush();

        // 部署
        appInstallService.deploy(appInstance, appComponentInstanceList);
        appInstanceRepository.saveAndFlush(appInstance);
        return buildSucceedResult("OK");

    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public TeslaBaseResult update(Long appInstanceId, @RequestBody AppDeployUpdateParam param)
        throws IOException, ApiException {
        AppPackage appPackage = appPackageRepository.findFirstById(param.getAppPackageId());

        // appInstance 更新
        AppInstance appInstance = appInstanceRepository.findFirstById(appInstanceId);
        param.patchAppInstance(appInstance, getUserEmployeeId());
        appInstanceRepository.saveAndFlush(appInstance);

        // appComponentInstanceList 获取
        List<AppComponent> appComponentList = appPackage.appComponentList();
        List<AppComponentInstance> appComponentInstanceList = param.toAppComponentInstanceList(
            appInstance, appComponentList, getUserEmployeeId());

        // 更新
        appUpdateService.update(appInstance, appComponentInstanceList, getUserEmployeeId());
        appInstanceRepository.saveAndFlush(appInstance);
        return buildSucceedResult("OK");

    }

    @ApiOperation(value = "卸载")
    @RequestMapping(value = "uninstall", method = RequestMethod.DELETE)
    public TeslaBaseResult uninstall(Long appInstanceId)
        throws IOException, ApiException {

        AppInstance appInstance = appInstanceRepository.findFirstById(appInstanceId);
        appInstanceRepository.deleteById(appInstanceId);
        appComponentInstanceRepository.deleteAllByAppInstanceId(appInstanceId);
        appUninstallService.uninstall(appInstance);
        return buildSucceedResult("OK");

    }

}
