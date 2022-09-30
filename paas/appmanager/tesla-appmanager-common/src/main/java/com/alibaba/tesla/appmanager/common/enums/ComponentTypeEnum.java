package com.alibaba.tesla.appmanager.common.enums;

/**
 * 组件类型 Enum
 *
 * @author qiuqiang.qq@alibaba-inc.com
 */
public enum ComponentTypeEnum {

    /**
     * 微服务 (已废弃)
     */
    MICROSERVICE,

    /**
     * K8S 微服务
     */
    K8S_MICROSERVICE,

    /**
     * HELM
     */
    HELM,

    /**
     * K8S JOB
     */
    K8S_JOB,

    /**
     * 资源 Addon
     */
    RESOURCE_ADDON,

    /**
     * 内置 Addon
     */
    INTERNAL_ADDON,

    /**
     * 运维特性 Addon
     */
    TRAIT_ADDON,

    /**
     * 自定义 Addon
     */
    CUSTOM_ADDON,

    /**
     * ABM Operator TVD
     */
    ABM_OPERATOR_TVD,

    /**
     * ABM-Chart
     */
    ABM_CHART,

    /**
     * ASI Component
     */
    ASI_COMPONENT,

    /**
     * ABM Kustomize Component
     */
    ABM_KUSTOMIZE,

    /**
     * ABM Helm Component
     */
    ABM_HELM,

    /**
     * ABM Status Component
     */
    ABM_STATUS,

    /**
     * ABM ES Status Component
     */
    ABM_ES_STATUS,

    /**
     * 脚本类型
     */
    SCRIPT;

    /**
     * 返回指定的 componentType 是否为 addon
     *
     * @param componentType Component Type
     * @return true or false
     */
    public static boolean isAddon(String componentType) {
        return RESOURCE_ADDON.toString().equals(componentType)
                || TRAIT_ADDON.toString().equals(componentType)
                || CUSTOM_ADDON.toString().equals(componentType);
    }

    /**
     * 返回指定的 componentType 是否为 K8S_MICROSERVICE || K8S_JOB
     *
     * @param componentType Component Type
     * @return true or false
     */
    public static boolean isMicroserviceOrJob(String componentType) {
        return K8S_MICROSERVICE.toString().equals(componentType) || K8S_JOB.toString().equals(componentType);
    }
}
