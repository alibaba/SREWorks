package com.alibaba.tesla.appmanager.plugin.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.autoconfig.PackageProperties;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.PluginKindEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.ClassUtil;
import com.alibaba.tesla.appmanager.common.util.PackageUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.common.util.ZipUtil;
import com.alibaba.tesla.appmanager.domain.core.ScriptIdentifier;
import com.alibaba.tesla.appmanager.domain.core.StorageFile;
import com.alibaba.tesla.appmanager.domain.req.PluginQueryReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.PluginDisableReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.PluginEnableReq;
import com.alibaba.tesla.appmanager.domain.schema.PluginDefinitionSchema;
import com.alibaba.tesla.appmanager.dynamicscript.repository.condition.DynamicScriptQueryCondition;
import com.alibaba.tesla.appmanager.dynamicscript.service.DynamicScriptService;
import com.alibaba.tesla.appmanager.dynamicscript.util.GroovyUtil;
import com.alibaba.tesla.appmanager.plugin.repository.PluginDefinitionRepository;
import com.alibaba.tesla.appmanager.plugin.repository.PluginTagRepository;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginDefinitionQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginTagQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginTagDO;
import com.alibaba.tesla.appmanager.plugin.service.PluginFrontendService;
import com.alibaba.tesla.appmanager.plugin.service.PluginService;
import com.alibaba.tesla.appmanager.server.storage.Storage;
import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

/**
 * Plugin 服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class PluginServiceImpl implements PluginService {

    /**
     * Definition Yaml 文件存储路径
     */
    private static final String DEFINITION_FILENAME = "definition.yaml";

    @Autowired
    private PackageProperties packageProperties;

    @Autowired
    private PluginDefinitionRepository pluginDefinitionRepository;

    @Autowired
    private PluginTagRepository pluginTagRepository;

    @Autowired
    private PluginFrontendService pluginFrontendService;

    @Autowired
    private DynamicScriptService dynamicScriptService;

    @Autowired
    private Storage storage;

    /**
     * 获取插件列表
     *
     * @param condition 查询插件列表请求
     * @return 插件列表
     */
    @Override
    public Pagination<PluginDefinitionDO> list(PluginDefinitionQueryCondition condition) {
        List<PluginDefinitionDO> records = pluginDefinitionRepository.selectByCondition(condition);
        return Pagination.valueOf(records, Function.identity());
    }

    /**
     * 启用指定插件
     *
     * @param request 插件启用请求
     * @return 开启后的 PluginDefinition 对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PluginDefinitionDO enable(PluginEnableReq request) {
        String pluginName = request.getPluginName();
        String pluginVersion = request.getPluginVersion();

        PluginDefinitionQueryCondition definitionCondition = PluginDefinitionQueryCondition.builder()
                .pluginName(pluginName)
                .pluginVersion(pluginVersion)
                .build();
        PluginDefinitionDO definitionDO = pluginDefinitionRepository.getByCondition(definitionCondition);
        if (definitionDO == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "plugin not found");
        } else if (definitionDO.getPluginRegistered()) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "plugin is already registered");
        }
        PluginDefinitionSchema definitionSchema = SchemaUtil.toSchema(
                PluginDefinitionSchema.class, definitionDO.getPluginSchema());
        if (!definitionDO.getPluginName().equals(definitionSchema.getPluginName())
                || !definitionDO.getPluginVersion().equals(definitionSchema.getPluginVersion())
                || !definitionDO.getPluginKind().equals(definitionSchema.getPluginKind().toString())) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "mismatch plugin definition schema, please check it");
        }

        // 注册前端资源 && Groovy
        StorageFile storageFile = new StorageFile(definitionDO.getPackagePath());
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory("plugin_enable");
            Path pluginZip = tmpDir.resolve("plugin.zip");
            String url = storage.getObjectUrl(storageFile.getBucketName(), storageFile.getObjectName(),
                    DefaultConstant.DEFAULT_FILE_EXPIRATION);
            FileUtils.copyURLToFile(new URL(url), pluginZip.toFile());
            ZipUtil.unzip(pluginZip.toString(), tmpDir.toString());
            FileUtils.deleteQuietly(pluginZip.toFile());
            pluginFrontendService.updateByPluginDefinition(definitionSchema, tmpDir);
            loadGroovyScripts(definitionSchema, tmpDir);
        } catch (IOException e) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "cannot create temp directory", e);
        } finally {
            if (tmpDir != null) {
                if (!tmpDir.toFile().delete()) {
                    log.error("delete temp directory failed|dir={}", tmpDir);
                }
            }
        }

        // 开启插件
        definitionDO.setPluginRegistered(true);
        int count = pluginDefinitionRepository.updateByCondition(definitionDO, definitionCondition);
        if (count != 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("register plugin failed, cannot update plugin definition|condition=%s",
                            JSONObject.toJSONString(definitionCondition)));
        }
        return definitionDO;
    }

    /**
     * 关闭指定插件
     *
     * @param request 插件关闭请求
     * @return 关闭后的 PluginDefinition 对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PluginDefinitionDO disable(PluginDisableReq request) {
        String pluginName = request.getPluginName();
        String pluginVersion = request.getPluginVersion();

        PluginDefinitionQueryCondition definitionCondition = PluginDefinitionQueryCondition.builder()
                .pluginName(pluginName)
                .pluginVersion(pluginVersion)
                .build();
        PluginDefinitionDO definitionDO = pluginDefinitionRepository.getByCondition(definitionCondition);
        if (definitionDO == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "plugin not found");
        }

        // 删除前端资源
        pluginFrontendService.deleteByPlugin(pluginName, pluginVersion);

        // 取消 Groovy 注册
        PluginDefinitionSchema schema = SchemaUtil.toSchema(
                PluginDefinitionSchema.class, definitionDO.getPluginSchema());
        PluginDefinitionSchema.Schematic schematic = schema.getSpec().getSchematic();
        if (schematic != null) {
            for (PluginDefinitionSchema.SchematicGroovyFile file : schematic.getGroovy().getFiles()) {
                if (request.isIgnoreGroovyFiles()) {
                    continue;
                }
                dynamicScriptService.removeScript(DynamicScriptQueryCondition.builder()
                        .kind(file.getKind())
                        .name(file.getName())
                        .build());
            }
        }

        // 关闭插件
        definitionDO.setPluginRegistered(false);
        int count = pluginDefinitionRepository.updateByCondition(definitionDO, definitionCondition);
        if (count != 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("register plugin failed, cannot update plugin definition|condition=%s",
                            JSONObject.toJSONString(definitionCondition)));
        }
        return definitionDO;
    }

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
        StorageFile storageFile = uploadPluginHistoryToStorage(pluginKind, pluginName,
                pluginVersion, pluginZipFile, force);

        // 写入 DB 记录
        PluginDefinitionDO record = update(definitionSchema, pluginKind, pluginName, pluginVersion,
                pluginDescription, pluginTags, storageFile);

        // 清理现场
        if (!pluginZipFile.toFile().delete()) {
            log.error("delete temp plugin zip file failed|pluginZipFile={}", pluginZipFile);
        }
        return record;
    }

    /**
     * 插入 Plugin Definition 信息到数据库
     *
     * @param definitionSchema  Definition Schema
     * @param pluginKind        插件类型
     * @param pluginName        插件名称
     * @param pluginVersion     插件版本
     * @param pluginDescription 插件描述
     * @param pluginTags        插件 Tags
     * @param storageFile       插件存储文件
     * @return PluginDefinitionDO 记录
     */
    @Transactional(rollbackFor = Exception.class)
    public PluginDefinitionDO update(
            PluginDefinitionSchema definitionSchema, PluginKindEnum pluginKind, String pluginName, String pluginVersion,
            String pluginDescription, List<String> pluginTags, StorageFile storageFile) {
        String logSuffix = String.format("pluginKind=%s|pluginName=%s|pluginVersion=%s|pluginTags=%s|packagePath=%s",
                pluginKind, pluginName, pluginVersion, JSONArray.toJSONString(pluginTags), storageFile.toPath());
        String pluginSchemaStr = SchemaUtil.toYamlMapStr(definitionSchema);

        PluginDefinitionQueryCondition condition = PluginDefinitionQueryCondition.builder()
                .pluginName(pluginName)
                .pluginVersion(pluginVersion)
                .build();
        PluginDefinitionDO definitionDO = pluginDefinitionRepository.getByCondition(condition);
        if (definitionDO == null) {
            definitionDO = PluginDefinitionDO.builder()
                    .pluginKind(pluginKind.toString())
                    .pluginName(pluginName)
                    .pluginVersion(pluginVersion)
                    .pluginRegistered(false)
                    .packagePath(storageFile.toPath())
                    .pluginDescription(pluginDescription)
                    .pluginDependencies(JSONArray.toJSONString(new JSONArray()))
                    .pluginSchema(pluginSchemaStr)
                    .build();
            int count = pluginDefinitionRepository.insert(definitionDO);
            if (count > 0) {
                log.info("plugin definition has inserted into database|{}|definitionSchema={}",
                        logSuffix, pluginSchemaStr);
            } else {
                log.error("insert plugin definition failed|{}|count=0", logSuffix);
            }
        } else if (definitionDO.getPluginRegistered()) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "the specified operation cloud not be completed " +
                    "because the plugin has been successfully registered and enabled");
        } else {
            definitionDO.setPluginKind(pluginKind.toString());
            definitionDO.setPluginRegistered(false);
            definitionDO.setPackagePath(storageFile.toPath());
            definitionDO.setPluginDescription(pluginDescription);
            definitionDO.setPluginDependencies(JSONArray.toJSONString(new JSONArray()));
            definitionDO.setPluginSchema(pluginSchemaStr);
            int count = pluginDefinitionRepository.updateByCondition(definitionDO, condition);
            if (count > 0) {
                log.info("plugin definition has updated in database|{}|definitionSchema={}",
                        logSuffix, pluginSchemaStr);
            } else {
                log.error("update plugin definition failed|{}|count=0", logSuffix);
            }
        }

        long pluginId = definitionDO.getId();
        int count = pluginTagRepository.deleteByCondition(PluginTagQueryCondition.builder().pluginId(pluginId).build());
        if (count > 0) {
            log.info("plugin tags have been cleaned up before installation|{}|count={}", logSuffix, count);
        }
        for (String pluginTag : pluginTags) {
            count = pluginTagRepository.insert(PluginTagDO.builder()
                    .pluginId(pluginId)
                    .tag(pluginTag)
                    .build());
            if (count > 0) {
                log.info("plugin tag has inserted into database|{}|pluginId={}|tag={}", logSuffix, pluginId, pluginTag);
            } else {
                log.error("insert plugin tag failed|{}|pluginId={}|tag={}|count=0", logSuffix, pluginId, pluginTag);
            }
        }
        return definitionDO;
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
    private StorageFile uploadPluginHistoryToStorage(
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

    /**
     * 加载全部 Groovy Scripts
     *
     * @param definitionSchema Plugin Definition Schema
     * @param pluginDir        插件 Zip 文件本地目录
     */
    private void loadGroovyScripts(PluginDefinitionSchema definitionSchema, Path pluginDir) {
        PluginKindEnum pluginKind = definitionSchema.getPluginKind();
        String pluginName = definitionSchema.getPluginName();
        String pluginVersion = definitionSchema.getPluginVersion();
        PluginDefinitionSchema.SchematicGroovy schematic = definitionSchema.getSpec().getSchematic().getGroovy();
        if (schematic == null) {
            log.info("no need to load groovy scripts in current plugin|pluginKind={}|pluginName={}|pluginVersion={}",
                    pluginKind, pluginName, pluginVersion);
            return;
        }

        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        for (PluginDefinitionSchema.SchematicGroovyFile file : schematic.getFiles()) {
            String fileKind = file.getKind();
            String fileName = file.getName();
            String filePath = file.getPath();
            String code;
            try {
                code = new String(Files.readAllBytes(Paths.get(pluginDir.toFile().toString(), filePath)));
            } catch (IOException e) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        "cannot read groovy code from local plugin zip", e);
            }

            // 校验 Groovy Code 是否合法
            ScriptIdentifier identifier;
            try {
                Class<?> clazz = groovyClassLoader.parseClass(code);
                identifier = GroovyUtil.getScriptIdentifierFromClass(clazz);
            } catch (AppException e) {
                throw e;
            } catch (Exception e) {
                throw new AppException(AppErrorCode.GROOVY_ERROR,
                        String.format("load groovy script failed|kind=%s|name=%s|path=%s",
                                fileKind, fileName, filePath), e);
            }
            if (!identifier.getKind().equals(fileKind) || !identifier.getName().equals(fileName)) {
                throw new AppException(AppErrorCode.GROOVY_ERROR,
                        String.format("mismatched kind/name in groovy script|definitionKind=%s|definitionName=%s|" +
                                        "codeKind=%s|codeName=%s", fileKind, fileName, identifier.getKind(),
                                identifier.getName()));
            }

            // 更新到 DB 中
            DynamicScriptQueryCondition condition = DynamicScriptQueryCondition.builder()
                    .kind(fileKind)
                    .name(fileName)
                    .build();
            dynamicScriptService.initScript(condition, identifier.getRevision(), code);
            log.info("groovy scripts has updated in dynamic script table|pluginKind={}|pluginName={}|" +
                            "pluginVersion={}|fileKind={}|fileName={}|filePath={}|code={}", pluginKind, pluginName,
                    pluginVersion, fileKind, fileName, filePath, code);
        }
    }
}
