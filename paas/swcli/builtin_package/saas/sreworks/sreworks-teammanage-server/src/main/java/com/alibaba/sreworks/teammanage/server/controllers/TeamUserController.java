package com.alibaba.sreworks.teammanage.server.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.common.util.StringUtil;
import com.alibaba.sreworks.domain.DO.TeamUser;
import com.alibaba.sreworks.domain.repository.TeamRepository;
import com.alibaba.sreworks.domain.repository.TeamUserRepository;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAuthproxyService;
import com.alibaba.sreworks.teammanage.server.params.TeamUserAddUserParam;
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
@RequestMapping("/teammanage/teamUser")
@Api(tags = "团队-人员")
public class TeamUserController extends BaseController {

    @Autowired
    TeamUserRepository teamUserRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    FlyadminAuthproxyService flyadminAuthproxyService;

    @ApiOperation(value = "list")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list(String name) {
        name = StringUtil.isEmpty(name) ? "" : name;
        List<JSONObject> ret = teamUserRepository.findObjectByUserAndNameLikeOrderByIdDesc(
            getUserEmployeeId(), "%" + name + "%");
        RegularUtil.underscoreToCamel(ret);
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "接触团队")
    @RequestMapping(value = "accessTeam", method = RequestMethod.PUT)
    public TeslaBaseResult accessTeam(Long teamId) {
        TeamUser teamUser = teamUserRepository.findFirstByTeamIdAndUser(teamId, getUserEmployeeId());
        teamUser.setGmtAccess(System.currentTimeMillis() / 1000);
        teamUserRepository.saveAndFlush(teamUser);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "最近使用团队")
    @RequestMapping(value = "recentAccessTeam", method = RequestMethod.GET)
    public TeslaBaseResult recentAccessTeam() {
        List<JSONObject> ret = teamUserRepository.findObjectByUserOrderByGmtAccessDesc(getUserEmployeeId(), 4);
        RegularUtil.underscoreToCamel(ret);
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "nameIdSelector")
    @RequestMapping(value = "nameIdSelector", method = RequestMethod.GET)
    public TeslaBaseResult nameIdSelector(String name) {
        name = StringUtil.isEmpty(name) ? "" : name;
        List<JSONObject> ret = teamUserRepository.findObjectByUserAndNameLikeOrderByIdDesc(
            getUserEmployeeId(), "%" + name + "%");
        return buildSucceedResult(JsonUtil.map(
            "options", ret.stream().map(jsonObject -> JsonUtil.map(
                "label", jsonObject.get("name"),
                "value", jsonObject.get("id")
            )).collect(Collectors.toList())
        ));
    }

    @ApiOperation(value = "listUser")
    @RequestMapping(value = "listUser", method = RequestMethod.GET)
    public TeslaBaseResult listUser(Long teamId) throws IOException, ApiException {
        Map<String, String> map = flyadminAuthproxyService.userEmpIdNameMap(getUserEmployeeId());
        List<JSONObject> ret = teamUserRepository.findAllByTeamId(teamId).stream()
            .map(TeamUser::toJsonObject)
            .peek(x -> x.put("userName", map.get(x.getString("user"))))
            .collect(Collectors.toList());
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "addUser")
    @RequestMapping(value = "addUser", method = RequestMethod.POST)
    public TeslaBaseResult addUser(Long teamId, @RequestBody TeamUserAddUserParam param) {
        TeamUser teamUser = new TeamUser(teamId, getUserEmployeeId());
        teamUser.setUser(param.getUser());
        teamUserRepository.saveAndFlush(teamUser);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "removeUser")
    @RequestMapping(value = "removeUser", method = RequestMethod.DELETE)
    public TeslaBaseResult removeUser(Long teamId, String user) throws Exception {
        Long count = teamUserRepository.countByTeamId(teamId);
        if (count <= 1) {
            throw new Exception("last user can not be remove");
        }
        teamUserRepository.deleteByTeamIdAndUser(teamId, user);
        return buildSucceedResult("OK");
    }

}
