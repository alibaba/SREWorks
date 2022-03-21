package com.alibaba.sreworks.appdev.server.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.appdev.server.params.AppComponentCreateParam;
import com.alibaba.sreworks.appdev.server.params.AppComponentModifyParam;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.domain.DO.AppComponent;
import com.alibaba.sreworks.domain.repository.AppComponentRepository;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAppmanagerComponentService;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;

import com.fasterxml.jackson.core.JsonProcessingException;
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
@RequestMapping("/appdev/appComponent")
@Api(tags = "应用组件")
public class AppComponentController extends BaseController {

    @Autowired
    FlyadminAppmanagerComponentService flyadminAppmanagerComponentService;

    @Autowired
    AppComponentRepository appComponentRepository;

    @ApiOperation(value = "创建")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public TeslaBaseResult create(Long appId, @RequestBody AppComponentCreateParam param)
        throws IOException, ApiException {
        AppComponent appComponent = param.toAppComponent(appId, getUserEmployeeId());
        Long exId = flyadminAppmanagerComponentService.create(
            appId, param.getName(), param.repo(), getUserEmployeeId());
        appComponent.setExId(exId);
        appComponentRepository.saveAndFlush(appComponent);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public TeslaBaseResult delete(Long id) throws IOException, ApiException {
        AppComponent appComponent = appComponentRepository.findFirstById(id);
        flyadminAppmanagerComponentService.delete(appComponent.getAppId(), appComponent.getExId(), getUserEmployeeId());
        appComponentRepository.deleteById(id);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "修改")
    @RequestMapping(value = "modify", method = RequestMethod.POST)
    public TeslaBaseResult modify(Long id, @RequestBody AppComponentModifyParam param) throws JsonProcessingException {
        AppComponent appComponent = appComponentRepository.findFirstById(id);
        param.patchAppComponent(appComponent, getUserEmployeeId());
        appComponentRepository.saveAndFlush(appComponent);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    public TeslaBaseResult get(Long id) {
        JSONObject ret = appComponentRepository.findFirstById(id).toJsonObject();
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "列表")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list(Long appId) {
        List<JSONObject> ret = appComponentRepository.findAllByAppId(appId).stream()
            .map(AppComponent::toJsonObject).collect(Collectors.toList());
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

}
