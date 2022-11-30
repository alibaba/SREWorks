package com.alibaba.tesla.appmanager.common.enums;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;

/**
 * 插件类型 Enum
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public enum PluginKindEnum {

    /**
     * Component
     */
    COMPONENT_DEFINITION("ComponentDefinition"),

    /**
     * Resource Addon
     */
    RESOURCE_ADDON_DEFINITION("ResourceAddonDefinition"),

    /**
     * Trait
     */
    TRAIT_DEFINITION("TraitDefinition"),

    /**
     * Policy
     */
    POLICY_DEFINITION("PolicyDefinition"),

    /**
     * Workflow Step
     */
    WORKFLOW_STEP_DEFINITION("WorkflowStepDefinition");

    private final String text;

    PluginKindEnum(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    /**
     * String to Enum
     *
     * @param text String
     * @return Enum
     */
    public static PluginKindEnum fromString(String text) {
        for (PluginKindEnum item : PluginKindEnum.values()) {
            if (item.text.equals(text)) {
                return item;
            }
        }
        throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                String.format("invalid plugin kind %s", text));
    }
}
