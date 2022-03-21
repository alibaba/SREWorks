package com.alibaba.sreworks.appcenter.server.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.appcenter.server.params.AppInstanceUpdateParam;
import com.alibaba.sreworks.appcenter.server.params.AppInstanceUpdateResourceParam;
import com.alibaba.sreworks.appdev.server.services.AppInstallService;
import com.alibaba.sreworks.appdev.server.services.AppUpdateService;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.common.util.YamlUtil;
import com.alibaba.sreworks.domain.DO.AppInstance;
import com.alibaba.sreworks.domain.repository.AppInstanceRepository;
import com.alibaba.sreworks.domain.repository.ClusterResourceRepository;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAuthproxyService;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/appcenter/appInstance")
@Api(tags = "应用实例")
public class AppInstanceController extends BaseController {

    @Autowired
    AppInstanceRepository appInstanceRepository;

    @Autowired
    AppInstallService appInstallService;

    @Autowired
    AppUpdateService appUpdateService;

    @Autowired
    FlyadminAuthproxyService flyadminAuthproxyService;

    @Autowired
    ClusterResourceRepository clusterResourceRepository;

    @ApiOperation(value = "我的列表")
    @RequestMapping(value = "listMy", method = RequestMethod.GET)
    public TeslaBaseResult listMy(String name) {
        name = StringUtils.isEmpty(name) ? "" : name;
        name = "%" + name + "%";
        List<JSONObject> ret = appInstanceRepository.findObjectByUser(getUserEmployeeId(), name);
        RegularUtil.underscoreToCamel(ret);
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "公共列表")
    @RequestMapping(value = "listPublic", method = RequestMethod.GET)
    public TeslaBaseResult listPublic(String name) {
        name = StringUtils.isEmpty(name) ? "" : name;
        name = "%" + name + "%";
        List<JSONObject> ret = appInstanceRepository.findPublicObject(name);
        RegularUtil.underscoreToCamel(ret);
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "get")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    public TeslaBaseResult get(Long id) throws IOException, ApiException {
        JSONObject ret = appInstanceRepository.getObjectById(id);
        Map<String, String> map = flyadminAuthproxyService.userEmpIdNameMap(getUserEmployeeId());
        ret.put("creator_name", map.get(ret.getString("creator")));
        RegularUtil.underscoreToCamel(ret);
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "getResource")
    @RequestMapping(value = "getResource", method = RequestMethod.GET)
    public TeslaBaseResult getResource(Long id) throws JsonProcessingException {
        AppInstance appInstance = appInstanceRepository.findFirstById(id);
        return buildSucceedResult(YamlUtil.toYaml(appInstance.detail().getResource().toJsonObject()));
    }

    @ApiOperation(value = "updateResource")
    @RequestMapping(value = "updateResource", method = RequestMethod.POST)
    public TeslaBaseResult updateResource(Long id, @RequestBody AppInstanceUpdateResourceParam param)
        throws IOException, ApiException {
        AppInstance appInstance = appInstanceRepository.findFirstById(id);
        param.patchAppInstance(appInstance, getUserEmployeeId());
        appInstanceRepository.saveAndFlush(appInstance);
        appUpdateService.replaceResourceQuota(appInstance);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "update")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public TeslaBaseResult update(Long id, @RequestBody AppInstanceUpdateParam param) {
        AppInstance appInstance = appInstanceRepository.findFirstById(id);
        param.patchAppInstance(appInstance, getUserEmployeeId());
        appInstanceRepository.saveAndFlush(appInstance);
        return buildSucceedResult("ok");
    }

    @ApiOperation(value = "getClusterResources")
    @RequestMapping(value = "getClusterResources", method = RequestMethod.GET)
    public TeslaBaseResult getClusterResources(Long id) {
        AppInstance appInstance = appInstanceRepository.findFirstById(id);
        return buildSucceedResult(appInstance.detail().clusterResourceIdList().stream()
            .map(clusterResourceId -> {
                JSONObject ret = clusterResourceRepository.findObjectById(clusterResourceId);
                RegularUtil.underscoreToCamel(ret);
                RegularUtil.gmt2Date(ret);
                return ret;
            })
            .collect(Collectors.toList()));
    }

}
