package com.alibaba.tesla.appmanager.domain.req.productrelease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品发布版本关联 App 记录更新请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReleaseAppUpdateReq {

    /**
     * 产品 ID
     */
    private String productId;

    /**
     * 发布版本 ID
     */
    private String releaseId;

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * TAG 覆盖 (可选)
     */
    private String tag;

    /**
     * 分支
     */
    private String branch;

    /**
     * 基线 BUILD YAML 路径
     */
    private String buildPath;

    /**
     * 基线 LAUNCH YAML 路径
     */
    private String launchPath;
}
