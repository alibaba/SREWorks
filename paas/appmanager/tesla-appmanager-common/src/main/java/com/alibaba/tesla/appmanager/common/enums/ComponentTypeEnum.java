package com.alibaba.tesla.appmanager.common.enums;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.google.common.base.Enums;
import org.checkerframework.checker.nullness.qual.Nullable;

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
     * ABM-Chart
     */
    ABM_CHART;

    public static ComponentTypeEnum parse(String value) {
        ComponentTypeEnum result = Enums.getIfPresent(ComponentTypeEnum.class, value).orNull();
        if (result == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid component type %s", value));
        }
        return result;
    }

    /**
     * 返回当前 component 类型是否为非 Addon
     *
     * @return true or false
     */
    public boolean isNotAddon() {
        return !RESOURCE_ADDON.equals(this) && !TRAIT_ADDON.equals(this) && !CUSTOM_ADDON.equals(this);
    }

    /**
     * 返回当前 component 类型是否为 Addon
     *
     * @return true or false
     */
    public boolean isAddon() {
        return RESOURCE_ADDON.equals(this) || INTERNAL_ADDON.equals(this);
    }

    /**
     * 返回当前 component 类型是否为 RESOURCE_ADDON
     *
     * @return true or false
     */
    public boolean isResourceAddon() {
        return RESOURCE_ADDON.equals(this);
    }

    /**
     * 返回当前 component 类型是否为 INTERNAL_ADDON
     *
     * @return true or false
     */
    public boolean isInternalAddon() {
        return INTERNAL_ADDON.equals(this);
    }

    /**
     * 返回当前 component 类型是否为 K8S_MICROSERVICE
     *
     * @return true or false
     */
    public boolean isKubernetesMicroservice() {
        return K8S_MICROSERVICE.equals(this);
    }

    /**
     * 返回当前 component 类型是否为 K8S_JOB
     *
     * @return true or false
     */
    public boolean isKubernetesJob() {
        return K8S_JOB.equals(this);
    }

    /**
     * 返回当前 component 类型是否为 HELM
     *
     * @return true or false
     */
    public boolean isHelm() {
        return HELM.equals(this);
    }
}
