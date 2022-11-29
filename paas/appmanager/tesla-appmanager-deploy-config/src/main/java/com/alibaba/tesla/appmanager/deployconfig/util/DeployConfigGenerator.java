package com.alibaba.tesla.appmanager.deployconfig.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;

/**
 * 部署配置生成器
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class DeployConfigGenerator {

    private final JSONObject config;

    public DeployConfigGenerator() {
        this.config = new JSONObject();
    }

    public DeployConfigGenerator addRevisionName(String componentType, String componentName) {
        config.put("revisionName", String.format("%s|%s|_", componentType, componentName));
        return this;
    }

    public DeployConfigGenerator addScope(String kind, String name) {
        config.putIfAbsent("scopes", new JSONArray());
        config.getJSONArray("scopes").add(new JSONObject());
        int scopeLength = config.getJSONArray("scopes").size();
        config.getJSONArray("scopes").getJSONObject(scopeLength - 1).put("scopeRef", new JSONObject());
        config.getJSONArray("scopes").getJSONObject(scopeLength - 1)
                .getJSONObject("scopeRef").put("apiVersion", "core.oam.dev/v1alpha2");
        config.getJSONArray("scopes").getJSONObject(scopeLength - 1)
                .getJSONObject("scopeRef").put("kind", kind);
        config.getJSONArray("scopes").getJSONObject(scopeLength - 1)
                .getJSONObject("scopeRef").put("name", name);
        return this;
    }

    public DeployConfigGenerator addDataInputs(JSONArray dataInputs) {
        if (dataInputs == null) {
            return this;
        }
        config.put("dataInputs", dataInputs);
        return this;
    }

    public DeployConfigGenerator addDataOutputs(JSONArray dataOutputs) {
        if (dataOutputs == null) {
            return this;
        }
        config.put("dataOutputs", dataOutputs);
        return this;
    }

    public String toString() {
        return SchemaUtil.createYaml(JSONObject.class).dumpAsMap(config);
    }
}
