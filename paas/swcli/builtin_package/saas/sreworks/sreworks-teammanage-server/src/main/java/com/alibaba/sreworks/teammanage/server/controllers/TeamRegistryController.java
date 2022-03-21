package com.alibaba.sreworks.teammanage.server.controllers;

import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.common.util.StringUtil;
import com.alibaba.sreworks.domain.DO.TeamRegistry;
import com.alibaba.sreworks.domain.repository.TeamRegistryRepository;
import com.alibaba.sreworks.teammanage.server.params.TeamRegistryCreateParam;
import com.alibaba.sreworks.teammanage.server.params.TeamRegistryModifyParam;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;

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
@RequestMapping("/teammanage/teamRegistry")
@Api(tags = "团队-镜像源")
public class TeamRegistryController extends BaseController {

    @Autowired
    TeamRegistryRepository teamRegistryRepository;

    @ApiOperation(value = "list")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list(String name) {
        name = StringUtil.isEmpty(name) ? "" : name;
        List<JSONObject> ret = teamRegistryRepository.findObjectByUser(getUserEmployeeId(), "%" + name + "%");
        RegularUtil.underscoreToCamel(ret);
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "get")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    public TeslaBaseResult get(Long id) {
        return buildSucceedResult(teamRegistryRepository.findFirstById(id));
    }

    @ApiOperation(value = "create")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public TeslaBaseResult create(@RequestBody TeamRegistryCreateParam param) {
        teamRegistryRepository.saveAndFlush(param.toTeamRegistry(getUserEmployeeId()));
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "delete")
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public TeslaBaseResult delete(Long id) {
        teamRegistryRepository.deleteById(id);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "modify")
    @RequestMapping(value = "modify", method = RequestMethod.POST)
    public TeslaBaseResult modify(Long id, @RequestBody TeamRegistryModifyParam param) {
        TeamRegistry teamRegistry = teamRegistryRepository.findFirstById(id);
        param.patchTeamRepo(teamRegistry, getUserEmployeeId());
        teamRegistryRepository.saveAndFlush(teamRegistry);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "idSelector")
    @RequestMapping(value = "idSelector", method = RequestMethod.GET)
    public TeslaBaseResult idSelector(Long teamId) {
        List<TeamRegistry> teamRegistryList = teamRegistryRepository.findAllByTeamId(teamId);
        return buildSucceedResult(JsonUtil.map(
            "options", teamRegistryList.stream().map(teamRegistry -> JsonUtil.map(
                "label", teamRegistry.getName(),
                "value", teamRegistry.getId()
            )).collect(Collectors.toList())
        ));
    }
}
