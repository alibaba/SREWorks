package com.alibaba.sreworks.job.master.services;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.common.JobApplicationProperties;
import com.alibaba.sreworks.job.master.domain.DTO.FlinkConnectorDTO;
import com.alibaba.sreworks.job.utils.Requests;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class VvpService {

    @Autowired
    JobApplicationProperties applicationProperties;

    public List<FlinkConnectorDTO> listConnector(Boolean isSource, Boolean isSink, String type) throws Exception {
        String connectorUrl = "http://prod-dataops-ververica-platform-ververica-platform.sreworks-dataops/sql/v1beta1/namespaces/default/connectors";
        HttpResponse<String> response = Requests.get(connectorUrl, null, null);
        JSONObject connectors = JSONObject.parseObject(response.body());

        Stream<JSONObject> connectorList = connectors.getJSONArray("connectors").stream()
                .filter(obj -> obj instanceof JSONObject)
                .map(obj -> (JSONObject) obj);

        if (isSource != null) {
            connectorList = connectorList.filter(item -> isSource && item.containsKey("source") && item.getBoolean("source"));
        }
        if (isSink != null) {
            connectorList = connectorList.filter(item -> isSink && item.containsKey("sink") && item.getBoolean("sink"));
        }
        if (type != null) {
            connectorList = connectorList.filter(item -> StringUtils.equals(type, item.getString("type")));
        }

        return connectorList.map(FlinkConnectorDTO::new).collect(Collectors.toList());
    }




}
