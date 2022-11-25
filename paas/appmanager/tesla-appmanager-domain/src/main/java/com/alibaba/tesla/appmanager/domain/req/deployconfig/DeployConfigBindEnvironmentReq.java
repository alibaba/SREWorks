package com.alibaba.tesla.appmanager.domain.req.deployconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 将指定应用绑定到指定环境
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployConfigBindEnvironmentReq {

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 隔离 Namespace ID
     */
    private String isolateNamespaceId;

    /**
     * 隔离 Stage ID
     */
    private String isolateStageId;

    /**
     * 环境 ID
     */
    private String envId;

    /**
     * 产品 ID
     */
    private String productId;

    /**
     * 发布版本 ID
     */
    private String releaseId;

    /**
     * 基线分支
     */
    private String baselineBranch;
}
