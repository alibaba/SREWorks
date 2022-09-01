package com.alibaba.tesla.appmanager.domain.req.plugin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 启用插件请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginEnableReq {

    /**
     * 插件名称
     */
    private String pluginName;

    /**
     * 插件版本
     */
    private String pluginVersion;
}
