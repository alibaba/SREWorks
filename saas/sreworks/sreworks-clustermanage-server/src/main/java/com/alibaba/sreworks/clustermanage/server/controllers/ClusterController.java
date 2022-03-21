package com.alibaba.sreworks.clustermanage.server.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.clustermanage.server.params.ClusterCreateParam;
import com.alibaba.sreworks.clustermanage.server.params.ClusterModifyParam;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.RegularUtil;
import com.alibaba.sreworks.common.util.StringUtil;
import com.alibaba.sreworks.domain.DO.Cluster;
import com.alibaba.sreworks.domain.repository.ClusterRepository;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAppmanagerClusterService;
import com.alibaba.sreworks.flyadmin.server.services.PluginClusterService;
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
@RequestMapping("/clustermanage/cluster")
@Api(tags = "集群")
public class ClusterController extends BaseController {

    @Autowired
    ClusterRepository clusterRepository;

    @Autowired
    PluginClusterService pluginClusterService;

    @Autowired
    FlyadminAppmanagerClusterService flyadminAppmanagerClusterService;

    @ApiOperation(value = "创建")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public TeslaBaseResult create(@RequestBody ClusterCreateParam param) throws IOException, ApiException {
        Cluster cluster = param.toCluster(getUserEmployeeId());
        String kubeConfig = pluginClusterService.getKubeConfig(
            cluster.getAccountId(), param.getClusterName(), getUserEmployeeId());
        cluster.setKubeconfig(kubeConfig);
        clusterRepository.saveAndFlush(cluster);
        flyadminAppmanagerClusterService.create(cluster, getUserEmployeeId());
        return buildSucceedResult(cluster.getId());
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public TeslaBaseResult delete(Long id) throws IOException, ApiException {
        clusterRepository.deleteById(id);
        flyadminAppmanagerClusterService.delete(id, getUserEmployeeId());
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "修改")
    @RequestMapping(value = "modify", method = RequestMethod.PUT)
    public TeslaBaseResult modify(Long id, @RequestBody ClusterModifyParam param) {
        Cluster cluster = clusterRepository.findFirstById(id);
        param.patchCluster(cluster, getUserEmployeeId());
        clusterRepository.saveAndFlush(cluster);
        return buildSucceedResult("OK");
    }

    @ApiOperation(value = "详情")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    public TeslaBaseResult get(Long id) {
        JSONObject ret = clusterRepository.findFirstById(id).toJsonObject();
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "列表")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public TeslaBaseResult list(String name) {
        name = StringUtil.isEmpty(name) ? "" : name;
        List<JSONObject> ret = clusterRepository.findObjectByUserAndNameLike(getUserEmployeeId(), "%" + name + "%");
        RegularUtil.underscoreToCamel(ret);
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "公共列表")
    @RequestMapping(value = "listPublic", method = RequestMethod.GET)
    public TeslaBaseResult listPublic(String name) {
        name = StringUtil.isEmpty(name) ? "" : name;
        List<JSONObject> ret = clusterRepository.findObjectByVisibleScopeIsPublicAndNameLike("%" + name + "%");
        RegularUtil.underscoreToCamel(ret);
        RegularUtil.gmt2Date(ret);
        return buildSucceedResult(ret);
    }

    @ApiOperation(value = "idSelector")
    @RequestMapping(value = "idSelector", method = RequestMethod.GET)
    public TeslaBaseResult idSelector(Long teamId) {
        List<Cluster> clusterList = clusterRepository.findAllByTeamId(teamId);
        return buildSucceedResult(JsonUtil.map(
            "options", clusterList.stream().map(cluster -> JsonUtil.map(
                "label", cluster.getName(),
                "value", cluster.getId()
            )).collect(Collectors.toList())
        ));
    }
}
