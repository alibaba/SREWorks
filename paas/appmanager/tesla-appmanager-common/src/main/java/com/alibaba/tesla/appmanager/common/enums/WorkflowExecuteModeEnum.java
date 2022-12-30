package com.alibaba.tesla.appmanager.common.enums;

/**
 * Workflow 运行模式枚举
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public enum WorkflowExecuteModeEnum {

    /**
     * 顺次执行
     */
    STEP_BY_STEP,

    /**
     * DAG 并发模式
     */
    DAG
}
