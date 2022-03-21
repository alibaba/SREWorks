package com.alibaba.tesla.appmanager.common.util;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabProject;

import java.io.IOException;

@Slf4j
public class GitlabUtil {
    public static GitlabProject createProject(String repoDomain, String repoGroup, String appName, String token) throws AppException {
        log.info(">>>gitlabUtil|createProject|enter|repoDomain={}, repoGroup={}, appName={}, token={}", repoDomain,
                repoGroup, appName, token);
        GitlabAPI api;
        try {
            api = GitlabAPI.connect(repoDomain, token);
        } catch (Exception e) {
            throw new AppException(AppErrorCode.GIT_ERROR, e.getMessage());
        }

        GitlabGroup group;
        try {
            group = api.getGroup(repoGroup);
        } catch (IOException e) {
            log.info(">>>gitlabUtil|getGroup|Err={}", e.getMessage(), e);
            throw new AppException(AppErrorCode.GIT_ERROR, e.getMessage());
        }

        GitlabProject project;
        try {
            project = api.getProject(repoGroup, appName);
        } catch (IOException e) {
            try {
                project = api.createProjectForGroup(appName, group);
            } catch (IOException e1) {
                log.info(">>>gitlabUtil|createProjectForGroup|Err={}", e.getMessage(), e);
                throw new AppException(AppErrorCode.GIT_ERROR, e1.getMessage());
            }
        }

        return project;
    }
}
