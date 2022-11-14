package com.alibaba.tesla.appmanager.plugin.util;

import com.alibaba.tesla.appmanager.common.constants.PatternConstant;

/**
 * Plugin 相关校验器
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class PluginValidator {

    /**
     * 校验 Plugin Name 是否合法
     *
     * @param pluginName Plugin Name
     * @return true or false
     */
    public static boolean validateName(String pluginName) {
        String[] array = pluginName.split("/");
        if (array.length != 2) {
            return false;
        }
        String name = array[0];
        String version = array[1];
        if (!name.matches(PatternConstant.DNS_REGEX)) {
            return false;
        }
        return version.matches(PatternConstant.ALPHANUMERIC_REGEX);
    }
}
