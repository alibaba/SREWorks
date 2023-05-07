package com.alibaba.sreworks.job.master.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.common.JobApplicationProperties;
import com.alibaba.sreworks.job.master.domain.DTO.FlinkConnectorDTO;
import com.alibaba.sreworks.job.utils.Requests;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class VvpService {

    @Autowired
    JobApplicationProperties applicationProperties;

    public List<FlinkConnectorDTO> listConnector(Boolean isSource, Boolean isSink, String type) throws Exception {
        String connectorUrl = String.format("%s/sql/v1beta1/namespaces/default/connectors", applicationProperties.getFlinkVvpEndpoint());
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

    public JSONObject uploadArtifact(String fileName, String fileContent) throws Exception {
        String dirName = "/tmp/uploadArtifact";

        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create the file and delete it if it already exists
        File file = new File(dir, fileName);
        if (file.exists()) {
            file.delete();
        }

        // Write the content to the file
        Path path = Paths.get(file.getAbsolutePath());
        Files.write(path, fileContent.getBytes());
        log.info("File created and written: " + file.getAbsolutePath());

        String uploadUrl = String.format("%s/artifacts/v1/namespaces/default/artifacts:upload", applicationProperties.getFlinkVvpEndpoint());

        HttpResponse<String> response = Requests.upload(file, uploadUrl, null, null);
        return JSONObject.parseObject(response.body());
    }

    public JSONObject listArtifacts() throws IOException, InterruptedException {

        String artifactsUrl = String.format("%s/artifacts/v1/namespaces/default/artifacts:list", applicationProperties.getFlinkVvpEndpoint());

        HttpResponse<String> response = Requests.get(artifactsUrl, null, null);
        return JSONObject.parseObject(response.body());

    }

    public JSONObject deployment(JSONObject metadata, JSONObject spec) throws Exception {
        String deploymentUrl = String.format("%s/api/v1/namespaces/default/deployments", applicationProperties.getFlinkVvpEndpoint());
        JSONObject payload = new JSONObject();
        payload.put("metadata", metadata);
        payload.put("spec", spec);
        HttpResponse<String> response = Requests.post(deploymentUrl, null, null, payload.toJSONString());
        return JSONObject.parseObject(response.body());
    }


}
