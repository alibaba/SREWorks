package com.alibaba.tesla.appmanager.domain.req.appenvironment;

import com.alibaba.tesla.appmanager.domain.container.AppComponentLocationContainer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 将指定应用绑定到指定环境
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppEnvironmentBindReq {

    /**
     * API Version
     */
    private String apiVersion;

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
     * 产品 ID
     */
    private String productId;

    /**
     * 发布版本 ID
     */
    private String releaseId;

    /**
     * 部署目标单元 ID
     */
    private String unitId;

    /**
     * 部署目标集群 ID
     */
    private String clusterId;

    /**
     * 部署目标 Namespace ID
     */
    private String namespaceId;

    /**
     * 部署目标 Stage ID
     */
    private String stageId;

    /**
     * 基线分支
     */
    private String baselineBranch;

    /**
     * 是否使用安全模式 (不进行同步 Git 仓库操作，仅在数据库中添加对应记录)
     */
    private boolean safeMode = false;

    /**
     * 应用对应的 Component 列表
     */
    private List<AppComponentLocationContainer> appComponents = new ArrayList<>();

    /**
     * 操作人员
     */
    private String operator;
}
