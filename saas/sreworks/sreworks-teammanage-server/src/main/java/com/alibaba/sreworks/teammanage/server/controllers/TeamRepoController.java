package com.alibaba.sreworks.teammanage.server.controllers;

import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.common.util.StringUtil;
import com.alibaba.sreworks.domain.DO.TeamRegistry;
import com.alibaba.sreworks.domain.DO.TeamRepo;
import com.alibaba.sreworks.domain.repository.TeamRepoRepository;
import com.alibaba.sreworks.teammanage.server.params.TeamRepoCreateParam;
import com.alibaba.sreworks.teammanage.server.params.TeamRepoModifyParam;
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
@RequestMapping("/teammanage/teamRepo")
@Api(tags = "团队-仓库")
public class TeamRepoController extends BaseController {

    @Autowired
    TeamRepoRepository teamRepoRepository;

    @ApiOperation(value = "list")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list(String name) {
        name = StringUtil.isEmpty(name) ? "" : name;
        List<JSONObject> ret = teamRepoRepository.findObjectByUser(getUserEmployeeId(), "%" + name + "%");
        RegularUtil.underscoreToCamel(ret);
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "get")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    public TeslaBaseResult get(Long id) {
        return buildSucceedResult(teamRepoRepository.findFirstById(id));
    }

    @ApiOperation(value = "create")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public TeslaBaseResult create(@RequestBody TeamRepoCreateParam param) {
        teamRepoRepository.saveAndFlush(param.toTeamRepo(getUserEmployeeId()));
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "delete")
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public TeslaBaseResult delete(Long id) {
        teamRepoRepository.deleteById(id);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "modify")
    @RequestMapping(value = "modify", method = RequestMethod.POST)
    public TeslaBaseResult modify(Long id, @RequestBody TeamRepoModifyParam param) {
        TeamRepo teamRepo = teamRepoRepository.findFirstById(id);
        param.patchTeamRepo(teamRepo, getUserEmployeeId());
        teamRepoRepository.saveAndFlush(teamRepo);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "idSelector")
    @RequestMapping(value = "idSelector", method = RequestMethod.GET)
    public TeslaBaseResult idSelector(Long teamId) {
        List<TeamRepo> teamRepoList = teamRepoRepository.findAllByTeamId(teamId);
        return buildSucceedResult(JsonUtil.map(
            "options", teamRepoList.stream().map(teamRepo -> JsonUtil.map(
                "label", teamRepo.getName(),
                "value", teamRepo.getId()
            )).collect(Collectors.toList())
        ));
    }
}
