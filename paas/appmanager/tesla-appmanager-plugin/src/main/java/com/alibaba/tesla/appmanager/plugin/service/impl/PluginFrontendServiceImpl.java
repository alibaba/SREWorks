package com.alibaba.tesla.appmanager.plugin.service.impl;

import com.alibaba.tesla.appmanager.common.enums.PluginKindEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.schema.PluginDefinitionSchema;
import com.alibaba.tesla.appmanager.plugin.repository.PluginFrontendRepository;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginFrontendQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginFrontendDO;
import com.alibaba.tesla.appmanager.plugin.service.PluginFrontendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class PluginFrontendServiceImpl implements PluginFrontendService {

    @Autowired
    private PluginFrontendRepository pluginFrontendRepository;

    /**
     * 获取指定的 Plugin Frontend 对象
     *
     * @param condition 查询条件
     * @return PluginFrontendDO
     */
    @Override
    public PluginFrontendDO get(PluginFrontendQueryCondition condition) {
        return pluginFrontendRepository.getByCondition(condition);
    }

    /**
     * 根据 Plugin Definition 更新所有 Frontend 记录
     *
     * @param definitionSchema Plugin Definition Schema
     * @param pluginDir        插件目录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByPluginDefinition(PluginDefinitionSchema definitionSchema, Path pluginDir) {
        PluginKindEnum pluginKind = definitionSchema.getPluginKind();
        String pluginName = definitionSchema.getPluginName();
        String pluginVersion = definitionSchema.getPluginVersion();
        PluginDefinitionSchema.SchematicFrontend schematic = definitionSchema.getSpec().getSchematic().getFrontend();
        if (schematic == null) {
            log.info("no need to import plugin frontend in current plugin|pluginKind={}|pluginName={}|pluginVersion={}",
                    pluginKind, pluginName, pluginVersion);
            return;
        }

        int count = pluginFrontendRepository.deleteByCondition(PluginFrontendQueryCondition.builder()
                .pluginName(pluginName)
                .pluginVersion(pluginVersion)
                .build());
        log.info("existing frontend plugin resources have been cleaned up|pluginName={}|pluginVersion={}|count={}",
                pluginName, pluginVersion, count);
        for (PluginDefinitionSchema.SchematicFrontendFile file : schematic.getFiles()) {
            String fileKind = file.getKind();
            String filePath = file.getPath();
            String config;
            try {
                config = new String(Files.readAllBytes(Paths.get(pluginDir.toFile().toString(), filePath)));
            } catch (IOException e) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        "cannot read frontend plugin resource from local plugin zip", e);
            }
            count = pluginFrontendRepository.insert(PluginFrontendDO.builder()
                    .pluginName(pluginName)
                    .pluginVersion(pluginVersion)
                    .name(fileKind)
                    .config(config)
                    .build());
            if (count > 0) {
                log.info("frontend plugin resource have been inserted into database|pluginName={}|pluginVersion={}|" +
                        "name={}|config={}", pluginName, pluginVersion, fileKind, config);
            } else {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, String.format("insert frontend plugin " +
                                "resource into database failed|pluginName=%s|pluginVersion=%s|name=%s|config=%s",
                        pluginName, pluginVersion, fileKind, config));
            }
        }
    }

    /**
     * 删除指定 PluginName + PluginVersion 下的全部 Frontend 资源
     *
     * @param pluginName    插件名称
     * @param pluginVersion 插件版本
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPlugin(String pluginName, String pluginVersion) {
        int count = pluginFrontendRepository.deleteByCondition(PluginFrontendQueryCondition.builder()
                .pluginName(pluginName)
                .pluginVersion(pluginVersion)
                .build());
        log.info("existing frontend plugin resources have been cleaned up|pluginName={}|pluginVersion={}|count={}",
                pluginName, pluginVersion, count);
    }
}
