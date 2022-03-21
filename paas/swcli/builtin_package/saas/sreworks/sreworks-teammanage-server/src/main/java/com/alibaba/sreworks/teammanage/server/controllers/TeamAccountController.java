package com.alibaba.sreworks.teammanage.server.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.common.util.StringUtil;
import com.alibaba.sreworks.domain.DO.TeamAccount;
import com.alibaba.sreworks.domain.repository.TeamAccountRepository;
import com.alibaba.sreworks.flyadmin.server.services.PluginAccountService;
import com.alibaba.sreworks.teammanage.server.params.TeamAccountCreateParam;
import com.alibaba.sreworks.teammanage.server.params.TeamAccountModifyParam;
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
@RequestMapping("/teammanage/teamAccount")
@Api(tags = "团队-账号")
public class TeamAccountController extends BaseController {

    @Autowired
    TeamAccountRepository teamAccountRepository;

    @Autowired
    PluginAccountService pluginAccountService;

    @ApiOperation(value = "创建")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public TeslaBaseResult create(Long teamId, @RequestBody TeamAccountCreateParam param) throws IOException {
        pluginAccountService.check(param.getType(), param.getDetail(), getUserEmployeeId());
        TeamAccount teamAccount = param.toTeamAccount(teamId, getUserEmployeeId());
        teamAccountRepository.saveAndFlush(teamAccount);
        return buildSucceedResult(teamAccount.getId());
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public TeslaBaseResult delete(Long id) {
        teamAccountRepository.deleteById(id);
        return buildSucceedResult("ok");
    }

    @ApiOperation(value = "修改")
    @RequestMapping(value = "modify", method = RequestMethod.PUT)
    public TeslaBaseResult modify(Long id, @RequestBody TeamAccountModifyParam param) throws IOException {
        pluginAccountService.check(param.getType(), param.getDetail(), getUserEmployeeId());
        TeamAccount teamAccount = teamAccountRepository.findFirstById(id);
        param.patchTeamAccount(teamAccount, getUserEmployeeId());
        teamAccountRepository.saveAndFlush(teamAccount);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "列表")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list(Long teamId, String name) {
        name = StringUtil.isEmpty(name) ? "" : name;
        List<TeamAccount> list = teamAccountRepository.findAllByTeamIdAndNameLikeOrderByIdDesc(
            teamId, "%" + name + "%");
        List<JSONObject> ret = list.stream().map(TeamAccount::toJsonObject).collect(Collectors.toList());
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "详情")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    public TeslaBaseResult get(Long id) {
        return buildSucceedResult(
            teamAccountRepository.findFirstById(id)
        );
    }

    @ApiOperation(value = "nameIdSelector")
    @RequestMapping(value = "nameIdSelector", method = RequestMethod.GET)
    public TeslaBaseResult nameIdSelector(Long teamId) {
        List<TeamAccount> teamAccountList = teamAccountRepository.findAllByTeamIdOrderByIdDesc(teamId);
        return buildSucceedResult(JsonUtil.map(
            "options", teamAccountList.stream().map(teamAccount -> JsonUtil.map(
                "label", teamAccount.getName(),
                "value", teamAccount.getId()
            )).collect(Collectors.toList())
        ));
    }

}
