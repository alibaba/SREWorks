package com.alibaba.tesla.appmanager.domain.dto;

import com.alibaba.tesla.appmanager.common.util.SecurityUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagePushDTO {
    /**
     *
     */
    private String dockerSecretName;

    /**
     * 镜像仓库
     */
    private ImagePushRegistryDTO imagePushRegistry;

    /**
     * 检查参数合法性
     */
    public void checkParameters() {
        SecurityUtil.checkInput(dockerSecretName);
        if (imagePushRegistry != null) {
            SecurityUtil.checkInput(imagePushRegistry.getDockerRegistry());
            SecurityUtil.checkInput(imagePushRegistry.getDockerNamespace());
        }
    }
}
