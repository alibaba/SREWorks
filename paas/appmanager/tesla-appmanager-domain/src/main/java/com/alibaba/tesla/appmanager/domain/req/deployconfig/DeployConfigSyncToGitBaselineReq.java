package com.alibaba.tesla.appmanager.domain.req.deployconfig;

import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 将当前的 Application 同步到指定仓库基线中
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployConfigSyncToGitBaselineReq {

    /**
     * 要同步的 Application 配置文件
     */
    private DeployAppSchema configuration;

    /**
     * CI Account
     */
    private String ciAccount;

    /**
     * CI Token
     */
    private String ciToken;

    /**
     * 镜像仓库地址
     */
    private String repo;

    /**
     * 分支地址
     */
    private String branch;

    /**
     * 文件目标路径
     */
    private String filePath;

    /**
     * 操作人员
     */
    private String operator;
}
