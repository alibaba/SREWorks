package com.alibaba.tesla.productops.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.productops.DO.ProductopsNode;
import com.alibaba.tesla.productops.common.StringUtil;
import com.alibaba.tesla.productops.repository.ProductopsNodeRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Data
@Slf4j
@Service
public class NodeAddUrlService {

    @Autowired
    ProductopsNodeRepository productopsNodeRepository;

    private String getUrl(ProductopsNode node, Map<String, ProductopsNode> nodeTypePathMap, String url) {
        if (node != null) {
            String name = JSONObject.parseObject(node.getConfig()).getString("name");
            url = StringUtil.isEmpty(url) ? name : name + "/" + url;
            return getUrl(nodeTypePathMap.get(node.getParentNodeTypePath()), nodeTypePathMap, url);
        } else {
            return url;
        }
    }

    public void addUrl(ProductopsNode node) {
        List<ProductopsNode> nodes = productopsNodeRepository.findAllByStageId(node.getStageId());
        Map<String, ProductopsNode> nodeTypePathMap = nodes.stream().collect(Collectors.toMap(
            ProductopsNode::getNodeTypePath, x -> x
        ));
        addUrl(node, nodeTypePathMap);
    }

    public void addUrl(ProductopsNode node, Map<String, ProductopsNode> nodeTypePathMap) {
        String appId = "";
        try {
            appId = node.getNodeTypePath().split("\\|")[0];
        } catch (Exception ignored) {

        }
        JSONObject config = JSONObject.parseObject(node.getConfig());
        config.put("url", appId + "/" + getUrl(node, nodeTypePathMap, ""));
        node.setConfig(JSONObject.toJSONString(config));
        productopsNodeRepository.saveAndFlush(node);
    }

    @Scheduled(fixedRate = 60000)
    public void addUrls() {
        List<ProductopsNode> nodes = productopsNodeRepository.findAll();
        Map<String, List<ProductopsNode>> stageIdNodesMap = new HashMap<>();
        for (ProductopsNode node : nodes) {
            if (!stageIdNodesMap.containsKey(node.getStageId())) {
                stageIdNodesMap.put(node.getStageId(), new ArrayList<>());
            }
            stageIdNodesMap.get(node.getStageId()).add(node);
        }
        for (String stageId : stageIdNodesMap.keySet()) {
            List<ProductopsNode> stageIdNodes = stageIdNodesMap.get(stageId);
            Map<String, ProductopsNode> nodeTypePathMap = stageIdNodes.stream().collect(Collectors.toMap(
                ProductopsNode::getNodeTypePath, x -> x
            ));
            for (ProductopsNode node : nodes) {
                addUrl(node, nodeTypePathMap);
            }
        }

    }

}
