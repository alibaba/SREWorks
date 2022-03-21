package com.alibaba.tesla.appmanager.domain.dto;

import com.alibaba.tesla.appmanager.common.enums.ContainerTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.RepoTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author qianmo.zm@alibaba-inc.com
 * @date 2020/11/26.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerObjectDTO {
    /**
     * 分类
     */
    private ContainerTypeEnum containerType;

    private String name;

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
     * 仓库路径
     */
    private String repoPath;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 开启Repo创建
     */
    private Boolean openCreateRepo;

    /**
     * 代码分支
     */
    private String branch;

    /**
     * Docker模板文件名称
     */
    private String dockerfileTemplate;

    /**
     * Docker模板文件变量
     */
    private List<ArgMetaDTO> dockerfileTemplateArgs;

    /**
     * build变量
     */
    private List<ArgMetaDTO> buildArgs;

    /**
     * 端口定义
     */
    private List<PortMetaDTO> ports;

    /**
     * 命令行定义
     */
    private String command;

    /**
     * 开发语言
     */
    private String language;

    /**
     * 服务类型
     */
    private String serviceType;

    public void initRepo() {
        int appNameIndex = repo.lastIndexOf("/");
        this.appName = repo.substring(appNameIndex + 1, repo.lastIndexOf("."));

        int repoGroupIndex = repo.lastIndexOf("/", appNameIndex - 1);
        this.repoGroup = repo.substring(repoGroupIndex + 1, appNameIndex);

        this.repoDomain = repo.substring(0, repoGroupIndex);
    }
}
