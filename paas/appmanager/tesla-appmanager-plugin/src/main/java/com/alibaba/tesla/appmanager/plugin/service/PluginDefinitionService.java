package com.alibaba.tesla.appmanager.plugin.service;

import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Plugin 服务接口
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface PluginDefinitionService {

    /**
     * 上传插件 (默认不启用)
     *
     * @param file  API 上传文件
     * @param force 是否强制上传覆盖
     * @return PluginDefinitionDO
     */
    PluginDefinitionDO upload(MultipartFile file, boolean force) throws IOException;
}
