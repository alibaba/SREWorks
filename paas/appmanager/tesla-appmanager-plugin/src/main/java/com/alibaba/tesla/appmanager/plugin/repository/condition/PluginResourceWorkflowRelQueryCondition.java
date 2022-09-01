package com.alibaba.tesla.appmanager.plugin.repository.condition;

import com.alibaba.tesla.appmanager.common.BaseCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Plugin Resource 关联 Workflow 查询条件
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PluginResourceWorkflowRelQueryCondition extends BaseCondition {

    private Long pluginResourceId;
    private String clusterId;
    private String workflowType;
}
