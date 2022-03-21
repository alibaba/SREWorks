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
 * @author qiuqiang.qq@alibaba-inc.com
 */
@Slf4j
@Component("Mysql57ResourceAddon")
public class Mysql57ResourceAddon extends BaseAddon {

    private static final String ENVIRONMENT = "environment";
    private static final String ENVIRONMENT_DAILY = "daily";
    private static final String ENVIRONMENT_PRODUCTION = "prod";

    @Getter
    private final ComponentTypeEnum addonType = ComponentTypeEnum.RESOURCE_ADDON;

    @Getter
    private final String addonId = "mysql";

    @Getter
    private final String addonVersion = "5.7";

    @Getter
    private final String addonLabel = "MySQL 5.7";

    @Getter
    private final String addonDescription = "MySQL 5.7";

    @Getter
    private final ComponentSchema addonSchema = SchemaUtil.toSchema(ComponentSchema.class, "\n" +
            "apiVersion: core.oam.dev/v1alpha2\n" +
            "kind: Component\n" +
            "metadata:\n" +
            "  annotations: {version: '5.7'}\n" +
            "  name: RESOURCE_ADDON.mysql\n" +
            "spec:\n" +
            "  workload:\n" +
            "    apiVersion: flyadmin.alibaba.com/v1alpha1\n" +
            "    kind: ResourceAddon\n" +
            "    metadata:\n" +
            "      annotations: {version: '5.7'}\n" +
            "      name: mysql\n" +
            "    spec:\n" +
            "      environment: ''\n" +
            "    dataOutputs:\n" +
            "      - {fieldPath: '{{ spec.mysqlHost }}', name: dbHost}\n" +
            "      - {fieldPath: '{{ spec.mysqlPort }}', name: dbPort}\n" +
            "      - {fieldPath: '{{ spec.mysqlUser }}', name: dbUser}\n" +
            "      - {fieldPath: '{{ spec.mysqlPassword }}', name: dbPassword}\n");

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
                        "mysqlHost", "rm-8vbh9q4l74qcz6t75.mysql.zhangbei.rds.aliyuncs.com",
                        "mysqlPort", "3306",
                        "mysqlUser", "superadmin",
                        "mysqlPassword", "qZfQ3g8WhxQQ"
                ));
                break;
            case ENVIRONMENT_PRODUCTION:
                schema.overwriteWorkloadSpecVars(ImmutableMap.of(
                        "mysqlHost", "rm-8vb15hwr5w4176891.mysql.zhangbei.rds.aliyuncs.com",
                        "mysqlPort", "3306",
                        "mysqlUser", "superadmin",
                        "mysqlPassword", "HHpEtX34k7Ms"
                ));
                break;
            default:
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "");
        }
        return schema;
    }
}
