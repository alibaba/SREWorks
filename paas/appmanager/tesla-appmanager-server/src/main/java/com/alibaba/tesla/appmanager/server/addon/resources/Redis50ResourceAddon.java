package com.alibaba.tesla.appmanager.server.addon.resources;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.AddonUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema;
import com.alibaba.tesla.appmanager.server.addon.BaseAddon;
import com.alibaba.tesla.appmanager.server.addon.req.ApplyAddonInstanceReq;
import com.alibaba.tesla.appmanager.server.event.loader.AddonLoadedEvent;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Redis 资源 Addon
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Component("Redis50ResourceAddon")
public class Redis50ResourceAddon extends BaseAddon {

    private static final String ENVIRONMENT = "environment";
    private static final String ENVIRONMENT_DAILY = "daily";
    private static final String ENVIRONMENT_PRODUCTION = "prod";

    @Getter
    private final ComponentTypeEnum addonType = ComponentTypeEnum.RESOURCE_ADDON;

    @Getter
    private final String addonId = "redis";

    @Getter
    private final String addonVersion = "5.0";

    @Getter
    private final String addonLabel = "Redis 5.0";

    @Getter
    private final String addonDescription = "Redis 5.0";

    @Getter
    private final ComponentSchema addonSchema = SchemaUtil.toSchema(ComponentSchema.class, "\n" +
            "apiVersion: core.oam.dev/v1alpha2\n" +
            "kind: Component\n" +
            "metadata:\n" +
            "  annotations: {version: '5.0'}\n" +
            "  name: RESOURCE_ADDON.redis\n" +
            "spec:\n" +
            "  workload:\n" +
            "    apiVersion: flyadmin.alibaba.com/v1alpha1\n" +
            "    kind: ResourceAddon\n" +
            "    metadata:\n" +
            "      annotations: {version: '5.0'}\n" +
            "      name: redis\n" +
            "    spec:\n" +
            "      environment: ''\n" +
            "    dataOutputs:\n" +
            "      - {fieldPath: '{{ spec.redisHost }}', name: redisHost}\n" +
            "      - {fieldPath: '{{ spec.redisPort }}', name: redisPort}\n" +
            "      - {fieldPath: '{{ spec.redisPassword }}', name: redisPassword}\n");

    @Getter
    private final String addonConfigSchema = "{\n" +
            "    \"schema\": {\n" +
            "        \"type\": \"object\",\n" +
            "        \"properties\": {\n" +
            "            \"common\": {\n" +
            "                \"type\": \"object\",\n" +
            "                \"properties\": {\n" +
            "                    \"environment\": {\n" +
            "                        \"x-component-props\": {\n" +
            "                            \"options\": [\n" +
            "                                {\n" +
            "                                    \"value\": \"daily\",\n" +
            "                                    \"label\": \"日常环境\"\n" +
            "                                },\n" +
            "                                {\n" +
            "                                    \"value\": \"pre\",\n" +
            "                                    \"label\": \"预发环境\"\n" +
            "                                },\n" +
            "                                {\n" +
            "                                    \"value\": \"product\",\n" +
            "                                    \"label\": \"线上环境\"\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        },\n" +
            "                        \"x-component\": \"Select\",\n" +
            "                        \"type\": \"string\",\n" +
            "                        \"description\": \"环境变量\",\n" +
            "                        \"title\": \"环境变量\"\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

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
        String environment = spec.getString(ENVIRONMENT);
        switch (environment) {
            case ENVIRONMENT_DAILY:
                schema.overwriteWorkloadSpecVars(ImmutableMap.of(
                        "redisHost", "r-8vb44ec1e87e3d24.redis.zhangbei.rds.aliyuncs.com",
                        "redisPort", "6379",
                        "redisPassword", "TeslaAdmin123456"
                ));
                break;
            case ENVIRONMENT_PRODUCTION:
                schema.overwriteWorkloadSpecVars(ImmutableMap.of(
                        "redisHost", "r-8vbd0ed43bd58a04.redis.zhangbei.rds.aliyuncs.com",
                        "redisPort", "6379",
                        "redisPassword", "LQr7FtRLv38G"
                ));
                break;
            default:
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "");
        }
        return schema;
    }
}
