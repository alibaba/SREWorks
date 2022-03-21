package com.alibaba.tesla.appmanager.server.addon.resources;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.util.AddonUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema;
import com.alibaba.tesla.appmanager.server.addon.BaseAddon;
import com.alibaba.tesla.appmanager.server.addon.req.ApplyAddonInstanceReq;
import com.alibaba.tesla.appmanager.server.event.loader.AddonLoadedEvent;
import com.alibaba.tesla.dag.common.Requests;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author qiuqiang.qq@alibaba-inc.com
 */
@Slf4j
@Component("CloudResourceAddon")
public class CloudResourceAddon extends BaseAddon {

    @Getter
    private final ComponentTypeEnum addonType = ComponentTypeEnum.RESOURCE_ADDON;

    @Getter
    private final String addonId = "cloudResource";

    @Getter
    private final String addonVersion = "1.0";

    @Getter
    private final String addonLabel = "cloudResource 1.0";

    @Getter
    private final String addonDescription = "云资源 1.0";

    @Override
    public ComponentSchema getAddonSchema() {
        return SchemaUtil.toSchema(ComponentSchema.class, "\n" +
                "apiVersion: core.oam.dev/v1alpha2\n" +
                "kind: Component\n" +
                "metadata:\n" +
                "  annotations: {version: '1.0'}\n" +
                "  name: RESOURCE_ADDON.cloudResource\n" +
                "spec:\n" +
                "  workload:\n" +
                "    apiVersion: flyadmin.alibaba.com/v1alpha1\n" +
                "    kind: ResourceAddon\n" +
                "    metadata:\n" +
                "      annotations: {version: '1.0'}\n" +
                "      name: cloudResource\n" +
                "    spec:\n" +
                "      connection: {}\n" +
                "    dataOutputs:\n" +
                "      - {fieldPath: '{{ spec.connection }}', name: connection}\n");
    }

    @Getter
    private final String addonConfigSchema = null;

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * 初始化，注册自身
     */
    @PostConstruct
    public void init() {
        publisher.publishEvent(new AddonLoadedEvent(
                this, AddonUtil.combineAddonKey(getAddonType(), getAddonId()), this.getClass().getSimpleName()));
    }

    @Override
    public ComponentSchema applyInstance(ApplyAddonInstanceReq request) {
        ComponentSchema schema = request.getSchema();
        JSONObject spec = (JSONObject) schema.getSpec().getWorkload().getSpec();
        log.info("APPLY_INSTANCE SPEC IN: " + JSONObject.toJSONString(spec));
        String type = spec.getString("type");
        String resourceType = spec.getString("resourceType");
        JSONObject account = spec.getJSONObject("account");
        JSONObject instance = spec.getJSONObject("instance");
        JSONObject attachParams = instance.getJSONObject("attachParams");
        String url = String.format(
                "http://11.164.62.97:27001/remoteResource/getInstanceUsageParameters?type=%s&resourceType=%s",
                type, resourceType
        );
        JSONObject postJson = new JSONObject();
        postJson.put("attachParams", attachParams);
        postJson.put("account", account);
        postJson.put("instance", instance);
        String postBody = JSONObject.toJSONString(postJson);
        String ret = null;
        try {
            ret = Requests.post(url, postBody, String.class);
            log.info("APPLY_INSTANCE RET: " + ret);
            schema.overwriteWorkloadSpecVars(ImmutableMap.of(
                    "connection", new YAMLMapper().writeValueAsString(JSONObject.parseObject(ret).getJSONObject("data"))
            ));
        } catch (Exception e) {
            throw new RuntimeException(String.format("url: %s postBody: %s ret: %s", url, postBody, ret), e);
        }
        return schema;
    }
}
