package com.alibaba.tesla.appmanager.common.constants;

/**
 * Workflow Context 中内置 Key 常量清单
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class WorkflowContextKeyConstant {

    /**
     * 部署单覆盖参数 (用于 apply-components 的上下过程连接)
     */
    public static final String DEPLOY_OVERWRITE_PARAMETERS = "_internal_overwrite_parameters";

    /**
     * 终止后续执行
     */
    public static final String CANCEL_EXECUTION = "_internal_cancel_execution";
}
