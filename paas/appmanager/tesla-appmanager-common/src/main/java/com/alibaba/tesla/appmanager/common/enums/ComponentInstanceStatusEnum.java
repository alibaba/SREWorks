package com.alibaba.tesla.appmanager.common.enums;

/**
 * 组件实例状态
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public enum ComponentInstanceStatusEnum {

    /**
     * 尚未实现
     */
    NOT_IMPLEMENTED,

    /**
     * 已过期
     */
    EXPIRED,

    /**
     * 进入等待状态
     */
    PENDING,

    /**
     * 正在运行中
     */
    RUNNING,

    /**
     * 准备升级中
     */
    PREPARING_UPDATE,

    /**
     * 升级中
     */
    UPDATING,

    /**
     * 准备删除中
     */
    PREPARING_DELETE,

    /**
     * 正在运行中，但出现 WARNING
     */
    WARNING,

    /**
     * 正在运行中，但出现 ERROR
     */
    ERROR,

    /**
     * 完成态（运行成功）
     */
    COMPLETED,

    /**
     * 完成态（运行失败）
     */
    FAILED,

    /**
     * 未知异常
     */
    UNKNOWN;
}
