package com.alibaba.tesla.appmanager.common.service;

import com.alibaba.tesla.appmanager.domain.dto.ContainerObjectDTO;
import com.alibaba.tesla.appmanager.domain.req.git.GitCloneReq;

import java.nio.file.Path;
import java.util.List;

public interface GitService {

    /**
     * Clone 仓库到指定目录中
     *
     * @param logContent 日志 StringBuilder
     * @param request    镜像构建请求
     * @param dir        目标存储目录
     */
    void cloneRepo(StringBuilder logContent, GitCloneReq request, Path dir);

    /**
     * 切换分支
     *
     * @param logContent 日志 StringBuilder
     * @param branch     分支
     * @param dir        目标存储目录
     */
    void checkoutBranch(StringBuilder logContent, String branch, Path dir);

    void createRepoList(List<ContainerObjectDTO> containerObjectList);
}
