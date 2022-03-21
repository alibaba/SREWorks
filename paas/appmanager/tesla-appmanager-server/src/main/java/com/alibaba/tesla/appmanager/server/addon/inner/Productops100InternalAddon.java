package com.alibaba.tesla.appmanager.server.addon.inner;

import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.util.AddonUtil;
import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema;
import com.alibaba.tesla.appmanager.server.addon.BaseAddon;
import com.alibaba.tesla.appmanager.server.addon.req.ApplyAddonInstanceReq;
import com.alibaba.tesla.appmanager.server.event.loader.AddonLoadedEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * ProductOps Addon
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Component("Productops100InternalAddon")
public class Productops100InternalAddon extends BaseAddon {

    @Getter
    private final ComponentTypeEnum addonType = ComponentTypeEnum.INTERNAL_ADDON;

    @Getter
    private final String addonId = "productops";

    @Getter
    private final String addonVersion = "1.0.0";

    @Getter
    private final String addonLabel = "Internal-ProductOps Addon";

    @Getter
    private final String addonDescription = "Internal-ProductOps Addon";

    @Getter
    private final ComponentSchema addonSchema = new ComponentSchema();

    @Getter
    private final String addonConfigSchema = "{\n" +
            "    \"schema\": {\n" +
            "        \"type\": \"object\",\n" +
            "        \"properties\": {\n" +
            "            \"common\": {\n" +
            "                \"type\": \"object\",\n" +
            "                \"properties\": {\n" +
            "                    \"endpoint\": {\n" +
            "                        \"x-component-props\": {\n" +
            "                            \"options\": [\n" +
            "                                {\n" +
            "                                    \"value\": \"http://productops-pre.internal.tesla.alibaba-inc.com\",\n" +
            "                                    \"label\": \"弹内 - 预发环境\"\n" +
            "                                },\n" +
            "                                {\n" +
            "                                    \"value\": \"http://productops.internal.tesla.alibaba-inc.com\",\n" +
            "                                    \"label\": \"弹内 - 生产环境\"\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        },\n" +
            "                        \"x-component\": \"Select\",\n" +
            "                        \"type\": \"string\",\n" +
            "                        \"description\": \"导出环境\",\n" +
            "                        \"title\": \"导出环境\"\n" +
            "                    },\n" +
            "                    \"namespaceId\": {\n" +
            "                        \"x-component-props\": {\n" +
            "                            \"options\": [\n" +
            "                                {\n" +
            "                                    \"value\": \"default\",\n" +
            "                                    \"label\": \"默认\"\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        },\n" +
            "                        \"x-component\": \"Select\",\n" +
            "                        \"type\": \"string\",\n" +
            "                        \"description\": \"Namespace\",\n" +
            "                        \"title\": \"Namespace\"\n" +
            "                    },\n" +
            "                    \"stageId\": {\n" +
            "                        \"x-component-props\": {\n" +
            "                            \"options\": [\n" +
            "                                {\n" +
            "                                    \"value\": \"pre\",\n" +
            "                                    \"label\": \"开发态\"\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        },\n" +
            "                        \"x-component\": \"Select\",\n" +
            "                        \"type\": \"string\",\n" +
            "                        \"description\": \"导出阶段\",\n" +
            "                        \"title\": \"导出阶段\"\n" +
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

    /**
     * 内置组件不需要做任何申请的事情
     *
     * @param request 创建请求
     * @return
     */
    @Override
    public ComponentSchema applyInstance(ApplyAddonInstanceReq request) {
        return request.getSchema();
    }
}
