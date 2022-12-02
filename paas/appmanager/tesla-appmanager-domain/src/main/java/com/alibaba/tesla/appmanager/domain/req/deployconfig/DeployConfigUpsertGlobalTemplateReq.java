package com.alibaba.tesla.appmanager.domain.req.deployconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeployConfig 更新全局模板请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployConfigUpsertGlobalTemplateReq {

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
