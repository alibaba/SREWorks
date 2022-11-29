package com.alibaba.tesla.appmanager.common.enums;

/**
 * 部署工单扩展属性类型 Enum
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public enum DeployAppAttrTypeEnum {

    /**
     * 应用配置
     */
    APP_CONFIGURATION,

    /**
     * 全局参数 (运行时)
     */
    GLOBAL_PARAMS,

    /**
     * 全局变量 (初始化)
     */
    GLOBAL_VARIABLES,

    /**
     * 初始化时提供的覆盖变量 (用于 GLOBAL_PARAMS 的覆盖)
     */
    OVERWRITE_PARAMS;
}
