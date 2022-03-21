package com.alibaba.tesla.productops.controllers;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.action.common.TeslaBaseResult;
import com.alibaba.tesla.action.controller.BaseController;
import com.alibaba.tesla.productops.DO.ProductopsNode;
import com.alibaba.tesla.productops.common.StringUtil;
import com.alibaba.tesla.productops.params.NodeDeleteParam;
import com.alibaba.tesla.productops.params.NodeInsertParam;
import com.alibaba.tesla.productops.params.NodeUpdateParam;
import com.alibaba.tesla.productops.repository.ProductopsNodeRepository;
import com.alibaba.tesla.productops.services.NodeAddUrlService;
import com.alibaba.tesla.productops.services.NodeService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jinghua.yjh
 */
@Slf4j
@RestController
@RequestMapping("/frontend/appTrees")
public class NodeController extends BaseController {

    @Autowired
    ProductopsNodeRepository productopsNodeRepository;

    @Autowired
    NodeService nodeService;

    @Autowired
    NodeAddUrlService nodeAddUrlService;

    private String getNodeTypePath(NodeInsertParam param) {
        String parentNodeTypePath = param.getParentNodeTypePath();
        String serviceType = param.getServiceType();
        if (StringUtil.isEmpty(parentNodeTypePath)) {
            return serviceType;
        }
        return parentNodeTypePath.endsWith(":") ?
            parentNodeTypePath + serviceType : parentNodeTypePath + "::" + serviceType;
    }

    @GetMapping(value = "/structures")
    public TeslaBaseResult get(String treeType, String appId, String stageId) {
        String nodeTypePath = appId + "|app|T:";
        JSONObject tree = nodeService.tree(nodeTypePath, appId, stageId);
        return buildSucceedResult(tree);
    }

    @PostMapping(value = "/structure")
    public TeslaBaseResult post(@RequestBody NodeInsertParam param, String stageId) {

        log.debug("POST: " + JSONObject.toJSONString(param));
        String nodeTypePath = getNodeTypePath(param);

        ProductopsNode node = ProductopsNode.builder()
            .gmtCreate(System.currentTimeMillis())
            .gmtModified(System.currentTimeMillis())
            .lastModifier(getUserEmployeeId())
            .stageId(stageId)
            .category(param.getCategory())
            .parentNodeTypePath(param.getParentNodeTypePath())
            .serviceType(param.getServiceType())
            .nodeTypePath(nodeTypePath)
            .version(param.getVersion())
            .config(JSONObject.toJSONString(param.getConfig()))
            .build();

        productopsNodeRepository.saveAndFlush(node);
        nodeAddUrlService.addUrl(node);
        return buildSucceedResult(node);

    }

    @PutMapping(value = "/structure")
    public TeslaBaseResult put(@RequestBody NodeUpdateParam param, String stageId) {
        if (log.isDebugEnabled()) {
            log.debug("PUT: " + JSONObject.toJSONString(param));
        }
        String[] words = param.getNodeTypePath().split(":");
        String serviceType = words[words.length - 1];

        ProductopsNode node = productopsNodeRepository.findFirstByNodeTypePathAndStageId(
            param.getNodeTypePath(), stageId);

        if (node == null) {
            node = ProductopsNode.builder()
                .gmtCreate(System.currentTimeMillis())
                .stageId(stageId)
                .serviceType(serviceType)
                .nodeTypePath(param.getNodeTypePath())
                .build();
        }
        List<String> roleList = param.subtractionRoleList(node);
        String parentNodeTypePath = param.getNodeTypePath().replace(":" + serviceType, "");
        parentNodeTypePath = StringUtils.strip(parentNodeTypePath, ":");
        if (!param.getNodeTypePath().endsWith(":") && !parentNodeTypePath.contains(":")) {
            parentNodeTypePath = parentNodeTypePath + ":";
        }
        node.setStageId(stageId);
        node.setParentNodeTypePath(parentNodeTypePath);
        node.setGmtModified(System.currentTimeMillis());
        node.setLastModifier(getUserEmployeeId());
        node.setVersion(param.getVersion());
        node.setConfig(JSONObject.toJSONString(param.getConfig()));
        productopsNodeRepository.saveAndFlush(node);
        nodeAddUrlService.addUrl(node);
        if (log.isDebugEnabled()) {
            log.debug(JSONObject.toJSONString(roleList));
        }
        for (String role : roleList) {
            nodeService.cleanNodesRole(node, role, stageId);
        }
        return buildSucceedResult("OK");

    }

    @DeleteMapping(value = "/structure")
    public TeslaBaseResult delete(@RequestBody NodeDeleteParam param, String stageId) {
        return buildSucceedResult(productopsNodeRepository
            .deleteByNodeTypePathAndStageId(param.getNodeTypePath(), stageId));
    }

    @DeleteMapping(value = "/cleanNodesRole")
    public TeslaBaseResult cleanNodesRole(String nodeTypePath, String role, String stageId) {
        ProductopsNode node = productopsNodeRepository.findFirstByNodeTypePathAndStageId(nodeTypePath, stageId);
        nodeService.cleanNodesRole(node, role, stageId);
        return buildSucceedResult("OK");
    }

}
