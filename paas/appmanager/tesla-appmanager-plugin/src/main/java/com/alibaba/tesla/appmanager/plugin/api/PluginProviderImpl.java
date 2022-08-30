package com.alibaba.tesla.appmanager.plugin.api;

import com.alibaba.tesla.appmanager.api.provider.PluginProvider;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.dto.PluginDefinitionDTO;
import com.alibaba.tesla.appmanager.plugin.assembly.PluginDefinitionDtoConvert;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import com.alibaba.tesla.appmanager.plugin.service.PluginDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Plugin Provider 实现
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class PluginProviderImpl implements PluginProvider {

    @Autowired
    private PluginDefinitionService pluginDefinitionService;

    @Autowired
    private PluginDefinitionDtoConvert pluginDefinitionDtoConvert;

    /**
     * 上传插件 (默认不启用)
     *
     * @param file  API 上传文件
     * @param force 是否强制上传覆盖
     * @return PluginDefinitionDTO
     */
    @Override
    public PluginDefinitionDTO upload(MultipartFile file, boolean force) {
        try {
            PluginDefinitionDO definition = pluginDefinitionService.upload(file, force);
            return pluginDefinitionDtoConvert.to(definition);
        } catch (IOException e) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "upload plugin failed", e);
        }
    }
}
