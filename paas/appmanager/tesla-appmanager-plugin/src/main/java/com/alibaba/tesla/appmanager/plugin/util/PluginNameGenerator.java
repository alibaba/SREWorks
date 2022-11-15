package com.alibaba.tesla.appmanager.plugin.util;

/**
 * Plugin 名称生成器
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class PluginNameGenerator {

    /**
     * 生成 Plugin Name
     *
     * @param pluginNamePrefix 前缀 (合法 DNS 名称)
     * @param pluginNameSuffix 后缀 (版本)
     * @return Plugin Name
     */
    public static String generate(String pluginNamePrefix, String pluginNameSuffix) {
        return pluginNamePrefix + "/" + pluginNameSuffix;
    }
}
