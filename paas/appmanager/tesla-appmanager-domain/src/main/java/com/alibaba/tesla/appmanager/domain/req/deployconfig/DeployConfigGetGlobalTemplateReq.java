package com.alibaba.tesla.appmanager.domain.req.deployconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取 DeployConfig 下的全局模板
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployConfigGetGlobalTemplateReq {

    /**
     * 隔离 Namespace
     */
    private String isolateNamespaceId;

    /**
     * 隔离 Stage
     */
    private String isolateStageId;

    /**
     * 环境 ID
     */
    private String envId;
}
