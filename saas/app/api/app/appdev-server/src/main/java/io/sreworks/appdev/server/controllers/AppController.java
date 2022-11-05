package io.sreworks.appdev.server.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sreworks.appdev.server.params.AppCreateParam;
import io.sreworks.appdev.server.params.AppModifyParam;
import io.sreworks.appdev.server.services.AppmanagerComponentService;
import io.sreworks.appdev.server.services.AppmanagerPackageService;
import io.sreworks.appdev.server.services.AppmanagerService;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.common.util.StringUtil;
import com.alibaba.sreworks.domain.DO.Action;
import com.alibaba.sreworks.domain.DO.App;
import com.alibaba.sreworks.domain.DO.Team;
import com.alibaba.sreworks.domain.repository.*;
import com.alibaba.sreworks.domain.services.SaveActionService;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAppmanagerAppService;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAppmanagerService;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAuthproxyUserService;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    AppmanagerService appmanagerService;

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

    @Autowired
    FlyadminAuthproxyUserService flyadminAuthproxyUserService;

    @Autowired
    SaveActionService saveActionService;

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    FlyadminAppmanagerService flyadminAppmanagerService;

    @Autowired
    AppmanagerComponentService appmanagerComponentService;

    @Autowired
    AppmanagerPackageService appmanagerPackageService;

    @Transactional(rollbackOn = Exception.class)
    @ApiOperation(value = "创建")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public TeslaBaseResult create(@RequestBody AppCreateParam param) throws Exception {
        App app = param.toApp(getUserEmployeeId());

        appRepository.saveAndFlush(app);
        if(param.getApiVersion() != null && param.getApiVersion().equals("v2")){
            appmanagerService.create(app);
        }else{
            flyadminAppmanagerAppService.create(app);
        }
        teamUserRepository.updateGmtAccessByTeamIdAndUser(param.getTeamId(), getUserEmployeeId());
        saveActionService.save(getUserEmployeeId(), "app", app.getId(), "创建应用");
        JSONObject result = new JSONObject();
        result.put("appDefId", app.getId());
        result.put("teamId", app.getTeamId());
        result.put("result", "OK");
        return buildSucceedResult(result);
    }

    @Transactional(rollbackOn = Exception.class)
    @ApiOperation(value = "删除")
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public TeslaBaseResult delete(Long id) throws Exception {
        saveActionService.save(getUserEmployeeId(), "app", id, "删除应用");
        App app = appRepository.findFirstById(id);
        int count = appInstanceRepository.countByAppId(id);
        if (count > 0) {
            throw new Exception("appInstance count: " + count + "; can not delete app");
        }
        log.info("app.getDisplay() " + app.getDisplay().toString());
        if (app.getDisplay() == 2) {
            log.info("appmanagerService.delete " + app.getName());
            appmanagerService.delete(app.getName(), getUserEmployeeId());
        }else {
            flyadminAppmanagerAppService.delete(id, getUserEmployeeId());
        }
        JSONObject result = new JSONObject();
        result.put("appDefId", app.getId());
        result.put("teamId", app.getTeamId());
        result.put("result", "OK");
        result.put("appId", app.getName());
        appRepository.deleteById(id);
        appComponentRepository.deleteByAppId(id);
        appPackageRepository.deleteByAppId(id);
        return buildSucceedResult(result);
    }

    @ApiOperation(value = "修改")
    @RequestMapping(value = "modify", method = RequestMethod.POST)
    public TeslaBaseResult modify(Long id, @RequestBody AppModifyParam param) throws Exception {
        saveActionService.save(getUserEmployeeId(), "app", id, "修改应用");
        App app = appRepository.findFirstById(id);
        param.patchApp(app, getUserEmployeeId());
        appRepository.saveAndFlush(app);
        if (app.getDisplay() == 2) {
            appmanagerService.update(app);
        }
        teamUserRepository.updateGmtAccessByTeamIdAndUser(app.getTeamId(), getUserEmployeeId());
        JSONObject result = new JSONObject();
        result.put("appDefId", app.getId());
        result.put("teamId", app.getTeamId());
        result.put("result", "OK");
        return buildSucceedResult(result);
    }

    @ApiOperation(value = "升级")
    @RequestMapping(value = "upgrade", method = RequestMethod.POST)
    public TeslaBaseResult upgrade(Long id) throws JsonProcessingException {
        App app = appRepository.findFirstById(id);
        app.setName("sreworks" + app.getId().toString());
        app.setDisplay(Long.valueOf(2));

        appRepository.saveAndFlush(app);
        JSONObject result = new JSONObject();
        result.put("appDefId", app.getId());
        result.put("teamId", app.getTeamId());
        result.put("result", "OK");
        return buildSucceedResult(result);
    }

    @ApiOperation(value = "详情")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    public TeslaBaseResult get(Long id) {
        JSONObject ret = appRepository.findFirstById(id).toJsonObject();
        RegularUtil.gmt2Date(ret);
        ret.put("detailDict", JSONObject.parseObject(ret.getString("detail")));
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "列表")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list(String name) {
        name = StringUtil.isEmpty(name) ? "" : name;
        List<JSONObject> list = appRepository.findObjectByUser(getUserEmployeeId(), "%" + name + "%");
        RegularUtil.underscoreToCamel(list);
        RegularUtil.gmt2Date(list);
        list.forEach(x -> x.put("detailDict", JSONObject.parseObject(x.getString("detail"))));
        list.forEach(x -> x.put("appId", x.getString("name")));
        list.forEach(x -> x.put("options", JSONObject.parseObject(x.getString("detail")).getJSONObject("options")));
        return buildSucceedResult(list);
    }

    @ApiOperation(value = "全部列表")
    @RequestMapping(value = "listAll", method = RequestMethod.GET)
    public TeslaBaseResult listAll() {
        List<JSONObject> list = appRepository.findAll().stream()
//            .filter(x -> 1L == x.getDisplay())
            .map(App::toJsonObject)
            .collect(Collectors.toList());
        RegularUtil.underscoreToCamel(list);
        RegularUtil.gmt2Date(list);
        list.forEach(x -> x.put("appId", x.getString("name")));
        list.forEach(x -> x.put("detailDict", JSONObject.parseObject(x.getString("detail"))));
        return buildSucceedResult(list);
    }

    @ApiOperation(value = "detail")
    @RequestMapping(value = "detail", method = RequestMethod.GET)
    public TeslaBaseResult detail(Long id, String appId) throws Exception {
        App app = appRepository.findFirstByName(appId);
        JSONObject ret = app.toJsonObject();
        Team team = teamRepository.findFirstById(app.getTeamId());
        ret.put("teamName", team.getName());
        flyadminAuthproxyUserService.patchNickName(ret, getUserEmployeeId(), "creator");
        RegularUtil.gmt2Date(ret);
        ret.put("detailDict", JSONObject.parseObject(ret.getString("detail")));
//        ret.put("appPackageCount", appPackageRepository.countByAppIdAndStatus(id, "SUCCESS"));
//        ret.put("appInstanceCount", appInstanceRepository.countByAppId(id));

        ret.put("appComponentCount", appmanagerComponentService.count(appId, getUserEmployeeId()));
        ret.put("appPackageCount", appmanagerPackageService.count(appId, getUserEmployeeId()));
        ret.put("appInstanceCount", 0);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "idSelector")
    @RequestMapping(value = "idSelector", method = RequestMethod.GET)
    public TeslaBaseResult idSelector(Long teamId) {
        List<App> appList = appRepository.findAllByTeamId(teamId);
        return buildSucceedResult(JsonUtil.map(
            "options", appList.stream().map(app -> JsonUtil.map(
                "label", app.getName(),
                "value", app.getId()
            )).collect(Collectors.toList())
        ));
    }

    @ApiOperation(value = "getAction")
    @RequestMapping(value = "getAction", method = RequestMethod.GET)
    public TeslaBaseResult getAction(Long id) throws IOException, ApiException {
        List<JSONObject> ret = actionRepository
            .findAllByTargetTypeAndTargetValueOrderByIdDesc("app", String.valueOf(id))
            .stream().map(Action::toJsonObject).collect(Collectors.toList());
        RegularUtil.gmt2Date(ret);
        flyadminAuthproxyUserService.patchNickName(ret, getUserEmployeeId(), "operator");
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "getAppInstanceAction")
    @RequestMapping(value = "getAppInstanceAction", method = RequestMethod.GET)
    public TeslaBaseResult getAppInstanceAction(Long appInstanceId) throws IOException, ApiException {
        List<JSONObject> ret = actionRepository
            .findAllByTargetTypeAndTargetValueOrderByIdDesc("appInstance", String.valueOf(appInstanceId))
            .stream().map(Action::toJsonObject).collect(Collectors.toList());
        RegularUtil.gmt2Date(ret);
        flyadminAuthproxyUserService.patchNickName(ret, getUserEmployeeId(), "operator");
        return buildSucceedResult(ret);
    }

//    @ApiOperation(value = "getComponents")
//    @RequestMapping(value = "getComponents", method = RequestMethod.GET)
//    public TeslaBaseResult getComponents(Long appId, @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp) throws IOException {
//        if (appId == null) {
//            return buildSucceedResult(new ArrayList<>());
//        }
//
//        String appmanagerAppId = "sreworks" + appId.toString();
//
//        String user = getUserEmployeeId();
//
//        JSONObject res = flyadminAppmanagerService.k8sMicroservice(headerBizApp, user, appmanagerAppId, "MICROSERVICE,K8S_MICROSERVICE");
//        JSONArray k8sMicroserviceList = res.getJSONArray("items");
//
//        log.info("{}", k8sMicroserviceList.toJSONString());
//
//        JSONObject resHelm = flyadminAppmanagerService.helm(headerBizApp, user, appmanagerAppId);
//        JSONArray helmList = resHelm.getJSONArray("items");
//
//        k8sMicroserviceList.addAll(helmList);
//
//        return buildSucceedResult(k8sMicroserviceList);
//    }

}
