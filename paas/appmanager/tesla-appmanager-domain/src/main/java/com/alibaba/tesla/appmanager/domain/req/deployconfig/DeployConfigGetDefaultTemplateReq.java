package com.alibaba.tesla.appmanager.domain.req.deployconfig;

import com.alibaba.tesla.appmanager.domain.container.AppComponentLocationContainer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取 DeployConfig 下的全局模板
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployConfigGetDefaultTemplateReq {

    /**
     * API Version
     */
    private String apiVersion;

    /**
     * 隔离 Namespace
     */
    private String isolateNamespaceId;

    /**
     * 隔离 Stage
     */
    private String isolateStageId;

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 单元 ID
     */
    private String unitId;

    /**
     * 集群 ID
     */
    private String clusterId;

    /**
     * Namespace ID
     */
    private String namespaceId;

    /**
     * Stage ID
     */
    private String stageId;

    /**
     * 应用对应的 Component 列表
     */
    private List<AppComponentLocationContainer> appComponents = new ArrayList<>();
}
