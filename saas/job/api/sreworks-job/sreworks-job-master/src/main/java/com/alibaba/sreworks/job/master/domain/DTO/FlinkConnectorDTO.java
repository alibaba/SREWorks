package com.alibaba.sreworks.job.master.domain.DTO;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.ElasticJobInstance;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
public class FlinkConnectorDTO {

    private String name;

    private String type;

    private Boolean packaged;

    private Boolean source;

    private Boolean sink;

    private Boolean lookup;

    private JSONArray supportedFormats;

    private JSONArray dependencies;

    private JSONArray properties;

    public FlinkConnectorDTO(JSONObject connectorObject) {
        this.name = connectorObject.getString("name");
        this.type = connectorObject.getString("type");
        this.packaged = connectorObject.getBoolean("packaged");
        this.source = connectorObject.getBoolean("source");
        this.sink = connectorObject.getBoolean("sink");
        this.lookup = connectorObject.getBoolean("lookup");
        this.supportedFormats = connectorObject.getJSONArray("supportedFormats");
        this.dependencies = connectorObject.getJSONArray("dependencies");
        this.properties = connectorObject.getJSONArray("properties");
    }

}
