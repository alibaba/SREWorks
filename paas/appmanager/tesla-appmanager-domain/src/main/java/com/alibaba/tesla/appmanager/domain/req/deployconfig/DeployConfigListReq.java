package com.alibaba.tesla.appmanager.domain.req.deployconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部署配置查询请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployConfigListReq {

    /**
     * ID
     */
    private Long id;

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 类型 ID
     */
    private String typeId;

    /**
     * 类型 ID 前缀
     */
    private String typeIdPrefix;

    /**
     * 环境 ID
     */
    private String envId;

    /**
     * API 版本
     */
    private String apiVersion;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否继承
     */
    private Boolean inherit;

    /**
     * Namespace ID
     */
    private String isolateNamespaceId;

    /**
     * Namespace ID 不等条件
     */
    private String isolateNamespaceIdNotEqualTo;

    /**
     * Stage ID
     */
    private String isolateStageId;

    /**
     * Stage ID 不等条件
     */
    private String isolateStageIdNotEqualTo;
}
