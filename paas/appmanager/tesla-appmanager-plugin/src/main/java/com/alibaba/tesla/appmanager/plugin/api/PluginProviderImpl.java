package com.alibaba.tesla.appmanager.plugin.api;

import com.alibaba.tesla.appmanager.api.provider.PluginProvider;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.PluginDefinitionDTO;
import com.alibaba.tesla.appmanager.domain.req.PluginQueryReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.PluginDisableReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.PluginEnableReq;
import com.alibaba.tesla.appmanager.plugin.assembly.PluginDefinitionDtoConvert;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import com.alibaba.tesla.appmanager.plugin.service.PluginService;
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
    private PluginService pluginService;

    @Autowired
    private PluginDefinitionDtoConvert pluginDefinitionDtoConvert;

    /**
     * 获取插件列表
     *
     * @param request 查询插件列表请求
     * @return 插件列表
     */
    @Override
    public Pagination<PluginDefinitionDTO> list(PluginQueryReq request) {
        Pagination<PluginDefinitionDO> records = pluginService.list(request);
        return Pagination.transform(records, record -> pluginDefinitionDtoConvert.to(record));
    }

    /**
     * 启用指定插件
     *
     * @param request 插件启用请求
     * @return 开启后的 PluginDefinition 对象
     */
    @Override
    public PluginDefinitionDTO enable(PluginEnableReq request) {
        PluginDefinitionDO definition = pluginService.enable(request);
        return pluginDefinitionDtoConvert.to(definition);
    }

    /**
     * 关闭指定插件
     *
     * @param request 插件关闭请求
     * @return 关闭后的 PluginDefinition 对象
     */
    @Override
    public PluginDefinitionDTO disable(PluginDisableReq request) {
        PluginDefinitionDO definition = pluginService.disable(request);
        return pluginDefinitionDtoConvert.to(definition);
    }

    /**
     * 上传插件 (默认不启用)
     *
     * @param file   API 上传文件
     * @param force  是否强制上传覆盖
     * @param enable 是否默认启用
     * @return PluginDefinitionDTO
     */
    @Override
    public PluginDefinitionDTO upload(MultipartFile file, boolean force, Boolean enable) {
        try {
            PluginDefinitionDO definition = pluginService.upload(file, force);
            definition = pluginService.enable(PluginEnableReq.builder()
                    .pluginName(definition.getPluginName())
                    .pluginVersion(definition.getPluginVersion())
                    .build());
            return pluginDefinitionDtoConvert.to(definition);
        } catch (IOException e) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "upload plugin failed", e);
        }
    }
}
