package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.domain.dto.PluginDefinitionDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Plugin Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface PluginProvider {

    /**
     * 上传插件 (默认不启用)
     *
     * @param file  API 上传文件
     * @param force 是否强制上传覆盖
     * @return PluginDefinitionDTO
     */
    PluginDefinitionDTO upload(MultipartFile file, boolean force);
}
