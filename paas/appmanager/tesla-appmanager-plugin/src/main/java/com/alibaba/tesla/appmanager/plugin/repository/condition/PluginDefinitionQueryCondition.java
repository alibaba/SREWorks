package com.alibaba.tesla.appmanager.plugin.repository.condition;

import com.alibaba.tesla.appmanager.common.BaseCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Plugin Definition 查询条件
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PluginDefinitionQueryCondition extends BaseCondition {

    private String pluginKind;
    private String pluginName;
    private String pluginVersion;
}
