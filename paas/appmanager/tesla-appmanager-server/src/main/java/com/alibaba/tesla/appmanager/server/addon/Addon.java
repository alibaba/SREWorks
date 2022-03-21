package com.alibaba.tesla.appmanager.server.addon;

import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema;
import com.alibaba.tesla.appmanager.server.addon.req.ApplyAddonInstanceReq;
import com.alibaba.tesla.appmanager.server.addon.req.ReleaseAddonInstanceReq;

/**
 * Addon 统一描述接口
 *
 * @author qiuqiang.qq@alibaba-inc.com
 */
public interface Addon {

    /**
     * 创建 Addon 实例，高耗时
     *
     * @param request 创建请求
     * @return dataOutput 数据
     */
    ComponentSchema applyInstance(ApplyAddonInstanceReq request);

    /**
     * 释放 Addon 实例
     *
     * @param request 释放请求
     */
    void releaseInstance(ReleaseAddonInstanceReq request);

    /**
     * 获取 Addon 唯一标识符
     *
     * @return addonId
     */
    String getAddonId();

    /**
     * 获取 Addon 版本号
     *
     * @return addonVersion
     */
    String getAddonVersion();

    /**
     * 获取 Addon Label
     *
     * @return addonLabel
     */
    String getAddonLabel();

    /**
     * 获取 Addon Description
     *
     * @return addonDescription
     */
    String getAddonDescription();

    /**
     * 获取 Addon 类型
     *
     * @return addonType
     */
    ComponentTypeEnum getAddonType();

    /**
     * 获取当前 Addon Schema 定义
     *
     * @return AddonSchema
     */
    ComponentSchema getAddonSchema();

    /**
     * 获取当前 Addon Schema中spec中的前端组件配置
     *
     * @return
     */
    String getAddonConfigSchema();
}
