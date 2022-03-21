package com.alibaba.sreworks.appdev.server.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.appdev.server.params.AppMarketSyncRemote2LocalParam;
import com.alibaba.sreworks.appdev.server.services.AppMarketService;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.domain.DO.AppPackage;
import com.alibaba.sreworks.domain.DO.Team;
import com.alibaba.sreworks.domain.repository.AppPackageRepository;
import com.alibaba.sreworks.domain.repository.TeamRepository;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAppmanagerMarketService;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;

import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
@RequestMapping("/appdev/appMarket")
@Api(tags = "应用市场")
public class AppMarketController extends BaseController {

    @Autowired
    FlyadminAppmanagerMarketService flyadminAppmanagerMarketService;

    @Autowired
    AppPackageRepository appPackageRepository;

    @Autowired
    AppMarketService appMarketService;

    @Autowired
    TeamRepository teamRepository;

    @ApiOperation(value = "list")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list() {
        List<AppPackage> appPackageList = appPackageRepository.findAllByOnSale(1);
        List<JSONObject> ret = appPackageList.parallelStream().map(appPackage -> {
            JSONObject jsonObject = appPackage.toJsonObject();
            Team team = teamRepository.findFirstById(appPackage.app().getTeamId());
            jsonObject.put("app", appPackage.app());
            jsonObject.put("appName", appPackage.app().getName());
            jsonObject.put("team", team);
            jsonObject.put("teamName", team.getName());
            jsonObject.put("appComponentList", appPackage.appComponentList());
            jsonObject.put("appComponentSize", appPackage.appComponentList().size());
            return jsonObject;
        }).collect(Collectors.toList());
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "download")
    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void download(HttpServletResponse response, HttpServletRequest request, Long appPackageId)
        throws IOException, ApiException {

        AppPackage appPackage = appPackageRepository.findFirstById(appPackageId);
        String zipFilePath = appMarketService.download(appPackage, getUserEmployeeId());

        response.setCharacterEncoding(request.getCharacterEncoding());
        response.setContentType("application/octet-stream");
        File file = new File(zipFilePath);
        FileInputStream fis = new FileInputStream(file);
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        IOUtils.copy(fis, response.getOutputStream());
        response.flushBuffer();
        fis.close();

    }

    @ApiOperation(value = "listRemote")
    @RequestMapping(value = "listRemote", method = RequestMethod.GET)
    public TeslaBaseResult listRemote() throws IOException {
        return buildSucceedResult(appMarketService.listRemote(getUserEmployeeId()));
    }

    @ApiOperation(value = "syncRemote2Local")
    @RequestMapping(value = "syncRemote2Local", method = RequestMethod.POST)
    public TeslaBaseResult syncRemote2Local(Long appPackageId,
        @RequestBody AppMarketSyncRemote2LocalParam param) throws Exception {
        appMarketService.syncRemote2Local(
            appPackageId,
            param.getTeamId(),
            param.getAppName(),
            getUserEmployeeId()
        );

        return buildSucceedResult("ok");
    }
}