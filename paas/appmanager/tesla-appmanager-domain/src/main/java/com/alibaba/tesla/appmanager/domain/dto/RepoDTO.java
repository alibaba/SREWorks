package com.alibaba.tesla.appmanager.domain.dto;

import com.alibaba.tesla.appmanager.common.enums.RepoTypeEnum;
import com.alibaba.tesla.appmanager.common.util.SecurityUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoDTO {
    /**
     * 仓库类型
     */
    private RepoTypeEnum repoType;

    /**
     * 仓库地址
     */
    private String repo;

    /**
     * CI Token
     */
    private String ciToken;

    /**
     * CI Account
     */
    private String ciAccount;

    /**
     * 仓库域名
     */
    private String repoDomain;

    /**
     * 仓库分组
     */
    private String repoGroup;

    /**
     * 仓库项目名
     */
    private String repoProject;

    /**
     * 仓库模板
     */
    private String repoTemplate;

    /**
     * 仓库模板URL
     */
    private String repoTemplateUrl;

    /**
     * 仓库路径
     */
    private String repoPath;

    /**
     * 仓库代码分支
     */
    private String repoBranch;

    /**
     * dockerfile路径
     */
    private String dockerfilePath;

    /**
     * 检查参数合法性
     */
    public void checkParameters() {
        SecurityUtil.checkInput(repo);
        SecurityUtil.checkInput(ciToken);
        SecurityUtil.checkInput(ciAccount);
        SecurityUtil.checkInput(repoDomain);
        SecurityUtil.checkInput(repoGroup);
        SecurityUtil.checkInput(repoProject);
        SecurityUtil.checkInput(repoTemplate);
        SecurityUtil.checkInput(repoTemplateUrl);
        SecurityUtil.checkInput(repoPath);
        SecurityUtil.checkInput(dockerfilePath);
        SecurityUtil.checkInput(repoBranch);
    }
}
