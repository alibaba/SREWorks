package com.alibaba.tesla.appmanager.domain.req.git;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * Git 更新文件请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitUpdateFileReq implements Serializable {

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * Git Clone 的目录，要求有 .git 目录
     */
    private Path cloneDir;

    /**
     * 需要更新的文件路径
     */
    private String filePath;

    /**
     * 文件内容
     */
    private String fileContent;

    /**
     * 操作人员
     */
    private String operator;
}
