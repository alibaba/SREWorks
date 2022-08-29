package com.alibaba.tesla.appmanager.plugin.service.impl;

import com.alibaba.tesla.appmanager.autoconfig.PackageProperties;
import com.alibaba.tesla.appmanager.common.enums.PluginKindEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.PackageUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.common.util.ZipUtil;
import com.alibaba.tesla.appmanager.domain.core.StorageFile;
import com.alibaba.tesla.appmanager.domain.schema.PluginDefinitionSchema;
import com.alibaba.tesla.appmanager.plugin.repository.PluginDefinitionRepository;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import com.alibaba.tesla.appmanager.plugin.service.PluginDefinitionService;
import com.alibaba.tesla.appmanager.server.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Plugin 服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class PluginDefinitionServiceImpl implements PluginDefinitionService {

    /**
     * Definition Yaml 文件存储路径
     */
    private static final String DEFINITION_FILENAME = "definition.yaml";

    @Autowired
    private PackageProperties packageProperties;

    @Autowired
    private PluginDefinitionRepository pluginDefinitionRepository;

    @Autowired
    private Storage storage;

    /**
     * 上传插件 (默认不启用)
     *
     * @param file  API 上传文件
     * @param force 是否强制上传覆盖
     * @return PluginDefinitionDO
     */
    @Override
    public PluginDefinitionDO upload(MultipartFile file, boolean force) throws IOException {
        Path pluginZipFile = Files.createTempFile("plugin", ".zip");
        Path pluginDir = Files.createTempDirectory("plugin");
        file.transferTo(pluginZipFile.toFile());
        ZipUtil.unzip(pluginZipFile.toFile().getAbsolutePath(), pluginDir.toFile().getAbsolutePath());
        log.info("plugin zip has unzipped to temp directory|dir={}", pluginDir);

        // 读取 definition 信息
        String definitionYaml = FileUtils.readFileToString(
                Paths.get(pluginDir.toFile().getAbsolutePath(), DEFINITION_FILENAME).toFile(),
                StandardCharsets.UTF_8);
        PluginDefinitionSchema definitionSchema = SchemaUtil.toSchema(PluginDefinitionSchema.class, definitionYaml);
        PluginKindEnum pluginKind = definitionSchema.getPluginKind();
        String pluginName = definitionSchema.getPluginName();
        String pluginVersion = definitionSchema.getPluginVersion();
        String pluginDescription = definitionSchema.getPluginDescription();
        List<String> pluginTags = definitionSchema.getPluginTags();

        // 上传当前 plugin 到远端存储
        StorageFile storageFile = uploadPluginHistory(pluginKind, pluginName, pluginVersion, pluginZipFile, force);

        // 写入 DB 记录
        PluginDefinitionDO record = writeDefinitionToDatabase(definitionSchema, pluginKind, pluginName,
                pluginDescription, pluginTags, storageFile);

        // 清理现场
        if (!pluginZipFile.toFile().delete()) {
            log.error("delete temp plugin zip file failed|pluginZipFile={}", pluginZipFile);
        }
        return record;
    }

    /**
     * 写入 Plugin Definition 信息到数据库
     *
     * @param definitionSchema  Definition Schema
     * @param pluginKind        插件类型
     * @param pluginName        插件名称
     * @param pluginDescription 插件描述
     * @param pluginTags        插件 Tags
     * @param storageFile       插件存储文件
     * @return PluginDefinitionDO 记录
     */
    @Transactional(rollbackFor = Exception.class)
    public PluginDefinitionDO writeDefinitionToDatabase(
            PluginDefinitionSchema definitionSchema, PluginKindEnum pluginKind, String pluginName,
            String pluginDescription, List<String> pluginTags, StorageFile storageFile) {
        return null;
    }

    /**
     * 上传当前 Plugin Zip 到远端存储
     *
     * @param pluginKind    插件类型
     * @param pluginName    插件名称
     * @param pluginVersion 插件版本
     * @param pluginZipFile 实际插件文件
     * @return 存储 StorageFile 文件
     */
    private StorageFile uploadPluginHistory(
            PluginKindEnum pluginKind, String pluginName, String pluginVersion, Path pluginZipFile, boolean force) {
        String bucketName = packageProperties.getBucketName();
        String objectName = PackageUtil.buildPluginHistoryRemotePath(pluginKind, pluginName, pluginVersion);
        if (storage.objectExists(bucketName, objectName) && !force) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("the plugin file already exists and cannot be overwritten|bucketName=%s|" +
                            "objectName=%s", bucketName, objectName));
        }
        storage.putObject(bucketName, objectName, pluginZipFile.toAbsolutePath().toString());
        log.info("plugin zip file has put into storage|pluginKind={}|pluginName={}|pluginVersion={}|bucketName={}|" +
                "objectName={}", pluginKind, pluginName, pluginVersion, bucketName, objectName);
        return StorageFile.builder()
                .bucketName(bucketName)
                .objectName(objectName)
                .build();
    }
}
