package com.alibaba.tesla.appmanager.plugin.api;

import com.alibaba.tesla.appmanager.api.provider.PluginProvider;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.ClassUtil;
import com.alibaba.tesla.appmanager.domain.dto.PluginDefinitionDTO;
import com.alibaba.tesla.appmanager.domain.dto.PluginFrontendDTO;
import com.alibaba.tesla.appmanager.domain.req.PluginQueryReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.*;
import com.alibaba.tesla.appmanager.plugin.assembly.PluginDefinitionDtoConvert;
import com.alibaba.tesla.appmanager.plugin.assembly.PluginFrontendDtoConvert;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginDefinitionQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginFrontendQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginFrontendDO;
import com.alibaba.tesla.appmanager.plugin.service.PluginFrontendService;
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
    private PluginFrontendService pluginFrontendService;

    @Autowired
    private PluginDefinitionDtoConvert pluginDefinitionDtoConvert;

    @Autowired
    private PluginFrontendDtoConvert pluginFrontendDtoConvert;

    /**
     * 获取插件列表
     *
     * @param request 查询插件列表请求
     * @return 插件列表
     */
    @Override
    public Pagination<PluginDefinitionDTO> list(PluginQueryReq request) {
        PluginDefinitionQueryCondition condition = new PluginDefinitionQueryCondition();
        ClassUtil.copy(request, condition);
        Pagination<PluginDefinitionDO> records = pluginService.list(condition);
        return Pagination.transform(records, record -> pluginDefinitionDtoConvert.to(record));
    }

    @Override
    public PluginDefinitionDTO get(PluginElementReq request) {
        PluginDefinitionQueryCondition condition = new PluginDefinitionQueryCondition();
        ClassUtil.copy(request, condition);
        return pluginDefinitionDtoConvert.to(pluginService.get(condition));
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
     * @param file    API 上传文件
     * @param request 上传插件请求
     * @return PluginDefinitionDTO
     */
    @Override
    public PluginDefinitionDTO upload(MultipartFile file, PluginUploadReq request) {
        try {
            PluginDefinitionDO definition = pluginService.upload(file, request.isOverwrite());
            if (request.isEnable()) {
                definition = pluginService.enable(PluginEnableReq.builder()
                        .pluginName(definition.getPluginName())
                        .pluginVersion(definition.getPluginVersion())
                        .build());
            }
            return pluginDefinitionDtoConvert.to(definition);
        } catch (IOException e) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "upload plugin failed", e);
        }
    }

    /**
     * 获取 Plugin Frontend 资源
     *
     * @param request 获取请求
     * @return PluginFrontend DTO 对象
     */
    @Override
    public PluginFrontendDTO getFrontend(PluginFrontendGetReq request) {
        PluginFrontendDO frontend = pluginFrontendService.get(PluginFrontendQueryCondition.builder()
                .pluginName(request.getPluginName())
                .pluginVersion(request.getPluginVersion())
                .name(request.getName())
                .build());
        return pluginFrontendDtoConvert.to(frontend);
    }
}
