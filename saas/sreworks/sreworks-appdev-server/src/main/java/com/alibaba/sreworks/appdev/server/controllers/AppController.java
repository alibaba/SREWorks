package com.alibaba.sreworks.appdev.server.controllers;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.appdev.server.params.AppCreateParam;
import com.alibaba.sreworks.appdev.server.params.AppModifyParam;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.common.util.StringUtil;
import com.alibaba.sreworks.domain.DO.App;
import com.alibaba.sreworks.domain.repository.AppComponentRepository;
import com.alibaba.sreworks.domain.repository.AppInstanceRepository;
import com.alibaba.sreworks.domain.repository.AppPackageRepository;
import com.alibaba.sreworks.domain.repository.AppRepository;
import com.alibaba.sreworks.domain.repository.TeamUserRepository;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAppmanagerAppService;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;

import com.fasterxml.jackson.core.JsonProcessingException;
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
@RequestMapping("/appdev/app")
@Api(tags = "应用开发")
public class AppController extends BaseController {

    @Autowired
    FlyadminAppmanagerAppService flyadminAppmanagerAppService;

    @Autowired
    AppRepository appRepository;

    @Autowired
    AppComponentRepository appComponentRepository;

    @Autowired
    TeamUserRepository teamUserRepository;

    @Autowired
    AppInstanceRepository appInstanceRepository;

    @Autowired
    AppPackageRepository appPackageRepository;

    @Transactional(rollbackOn = Exception.class)
    @ApiOperation(value = "创建")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public TeslaBaseResult create(@RequestBody AppCreateParam param) throws Exception {
        App app = param.toApp(getUserEmployeeId());
        appRepository.saveAndFlush(app);
        flyadminAppmanagerAppService.create(app);
        teamUserRepository.updateGmtAccessByTeamIdAndUser(param.getTeamId(), getUserEmployeeId());
        return buildSucceedResult(app.getId());
    }

    @Transactional(rollbackOn = Exception.class)
    @ApiOperation(value = "删除")
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public TeslaBaseResult delete(Long id) throws Exception {
        int count = appInstanceRepository.countByAppId(id);
        if (count > 0) {
            throw new Exception("appInstance count: " + count + "; can not delete app");
        }
        flyadminAppmanagerAppService.delete(id, getUserEmployeeId());
        appRepository.deleteById(id);
        appComponentRepository.deleteByAppId(id);
        appPackageRepository.deleteByAppId(id);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "修改")
    @RequestMapping(value = "modify", method = RequestMethod.POST)
    public TeslaBaseResult modify(Long id, @RequestBody AppModifyParam param) throws JsonProcessingException {
        App app = appRepository.findFirstById(id);
        param.patchApp(app, getUserEmployeeId());
        appRepository.saveAndFlush(app);
        teamUserRepository.updateGmtAccessByTeamIdAndUser(app.getTeamId(), getUserEmployeeId());
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "详情")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    public TeslaBaseResult get(Long id) {
        JSONObject ret = appRepository.findFirstById(id).toJsonObject();
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "列表")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list(String name) {
        name = StringUtil.isEmpty(name) ? "" : name;
        List<JSONObject> list = appRepository.findObjectByUser(getUserEmployeeId(), "%" + name + "%");
        RegularUtil.underscoreToCamel(list);
        RegularUtil.gmt2Date(list);
        return buildSucceedResult(list);
    }

    @ApiOperation(value = "全部列表")
    @RequestMapping(value = "listAll", method = RequestMethod.GET)
    public TeslaBaseResult listAll() {
        List<JSONObject> list = appRepository.findAll().stream()
            .map(App::toJsonObject)
            .collect(Collectors.toList());
        RegularUtil.underscoreToCamel(list);
        RegularUtil.gmt2Date(list);
        return buildSucceedResult(list);
    }

}
