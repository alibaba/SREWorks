package com.alibaba.tesla.appmanager.server.service.componentpackage.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.autoconfig.PackageProperties;
import com.alibaba.tesla.appmanager.common.enums.ComponentActionEnum;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.CommandUtil;
import com.alibaba.tesla.appmanager.common.util.PackageUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.common.util.StringUtil;
import com.alibaba.tesla.appmanager.domain.core.StorageFile;
import com.alibaba.tesla.appmanager.domain.req.componentpackage.BuildComponentHandlerReq;
import com.alibaba.tesla.appmanager.domain.res.componentpackage.LaunchBuildComponentHandlerRes;
import com.alibaba.tesla.appmanager.dynamicscript.core.GroovyHandlerFactory;
import com.alibaba.tesla.appmanager.server.event.componentpackage.SucceedComponentPackageTaskEvent;
import com.alibaba.tesla.appmanager.server.repository.ComponentPackageRepository;
import com.alibaba.tesla.appmanager.server.repository.ComponentPackageTaskRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.ComponentPackageTaskQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.ComponentPackageDO;
import com.alibaba.tesla.appmanager.server.repository.domain.ComponentPackageTaskDO;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageBuilderService;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageBuilderExecutorManager;
import com.alibaba.tesla.appmanager.server.service.componentpackage.handler.BuildComponentHandler;
import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.ComponentPackageBase;
import com.alibaba.tesla.appmanager.server.storage.Storage;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

@Service
@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ComponentPackageBuilderServiceImpl implements ComponentPackageBuilderService {

    @Autowired
    private PackageProperties packageProperties;
    @Autowired
    private ComponentPackageBuilderExecutorManager componentPackageBuilderExecutorManager;
    @Autowired
    private ComponentPackageTaskRepository componentPackageTaskRepository;
    @Autowired
    private ComponentPackageRepository componentPackageRepository;
    @Autowired
    private Storage storage;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private GroovyHandlerFactory groovyHandlerFactory;

    /**
     * ?????????????????? Component Package
     *
     * @param request ComponentPackage ??????????????????
     * @return ???????????????
     */
    @Override
    public LaunchBuildComponentHandlerRes build(BuildComponentHandlerReq request) throws IOException {
        ComponentTypeEnum componentType = Enums
                .getIfPresent(ComponentTypeEnum.class, request.getComponentType()).orNull();
        if (componentType == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "null componentType");
        }
        String componentName = request.getComponentName();

        // ?????? Groovy ?????????????????????????????????????????????????????????
        BuildComponentHandler handler = groovyHandlerFactory.getByComponentType(
                BuildComponentHandler.class, request.getAppId(), componentType, componentName, ComponentActionEnum.BUILD);
        if (handler == null) {
            return compatibleBuild(request);
        } else {
            return handler.launch(request);
        }
    }

    /**
     * ????????????????????? abmcli ????????????????????????
     *
     * @param request ComponentPackage ??????????????????
     * @return ???????????????
     * @throws IOException
     */
    private LaunchBuildComponentHandlerRes compatibleBuild(BuildComponentHandlerReq request) throws IOException {
        String appId = request.getAppId();
        String componentType = request.getComponentType();
        String componentName = request.getComponentName();
        String packageVersion = request.getVersion();
        JSONObject packageOptions = request.getOptions();

        // ????????? Map
        Map<String, Object> metaMap = ImmutableMap.of(
                "appId", appId,
                "version", packageVersion,
                "componentType", componentType,
                "componentName", componentName,
                "options", packageOptions
        );
        Yaml yaml = SchemaUtil.createYaml(Arrays.asList(Map.class, Object.class));
        String metaYaml = yaml.dumpAsMap(metaMap);

        // ????????????????????????
        File configFile, targetFile;
        configFile = File.createTempFile("component_package_task", ".yaml");
        targetFile = File.createTempFile("component_package_task", ".zip");
        configFile.deleteOnExit();
        targetFile.deleteOnExit();
        FileUtils.writeStringToFile(configFile, metaYaml, StandardCharsets.UTF_8);
        String configFilePath = configFile.toPath().toString();
        String targetFilePath = targetFile.toPath().toString();

        // ??????????????????
        String command = String.format("%s saas-package export-component-package --config-file-path %s " +
                "--creator SYSTEM --target-path %s", packageProperties.getAbmcliBin(), configFilePath, targetFilePath);
        String output = CommandUtil.runLocalCommand(command);
        String targetFileMd5 = StringUtil.getMd5Checksum(targetFilePath);
        String targetFileMeta = PackageUtil.getComponentPackageMeta(targetFilePath);
        log.info("export component package task has executed|command={}|md5={}", command, targetFileMd5);

        // ?????????????????? Storage ???
        String bucketName = packageProperties.getBucketName();
        String remotePath = PackageUtil
                .buildComponentPackageRemotePath(appId, componentType, componentName, packageVersion);
        storage.putObject(bucketName, remotePath, targetFilePath);
        log.info("component package has uploaded to storage|bucketName={}|remotePath={}|localPath={}",
                bucketName, remotePath, targetFilePath);

        // ?????????????????? (???????????????)
        if (!configFile.delete()) {
            log.warn("cannot delete component package config file {}", configFile.toPath());
        }
        if (!targetFile.delete()) {
            log.warn("cannot delete component package target file {}", targetFile.toPath());
        }

        return LaunchBuildComponentHandlerRes.builder()
                .logContent(output)
                .storageFile(new StorageFile(bucketName, remotePath))
                .packageMetaYaml(targetFileMeta)
                .packageMd5(targetFileMd5)
                .build();
    }

    /**
     * ?????? kaniko ???????????? Component Package
     *
     * @param taskDO
     * @return
     */
    @Override
    public void kanikoBuild(ComponentPackageTaskDO taskDO) throws Exception {
        ComponentTypeEnum componentType = Enums
                .getIfPresent(ComponentTypeEnum.class, taskDO.getComponentType()).orNull();
        if (componentType == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "null componentType");
        }
        String componentName = taskDO.getComponentName();
        BuildComponentHandlerReq componentHandlerReq = BuildComponentHandlerReq.builder()
                .appId(taskDO.getAppId())
                .namespaceId(taskDO.getNamespaceId())
                .stageId(taskDO.getStageId())
                .componentType(taskDO.getComponentType())
                .componentName(taskDO.getComponentName())
                .version(taskDO.getPackageVersion())
                .options(JSONObject.parseObject(taskDO.getPackageOptions()))
                .build();

        switch (componentType) {
            case K8S_MICROSERVICE:
            case K8S_JOB: {
                ComponentPackageBase instance = componentPackageBuilderExecutorManager.getInstance(componentType.name());
                instance.exportComponentPackage(taskDO);
                break;
            }
            default:
                BuildComponentHandler handler = groovyHandlerFactory.getByComponentType(BuildComponentHandler.class,
                        componentHandlerReq.getAppId(), componentType, componentName, ComponentActionEnum.BUILD);
                LaunchBuildComponentHandlerRes res;
                if (handler == null) {
                    res = compatibleBuild(componentHandlerReq);
                } else {
                    res = handler.launch(componentHandlerReq);
                }
                storeAndPublish(taskDO, res);
                break;
        }
    }

    private void storeAndPublish(ComponentPackageTaskDO taskDO, LaunchBuildComponentHandlerRes infoBO) {
        // ?????? Component Package ?????????
        ComponentPackageDO componentPackageDO = ComponentPackageDO.builder()
                .appId(taskDO.getAppId())
                .componentType(taskDO.getComponentType())
                .componentName(taskDO.getComponentName())
                .packageVersion(taskDO.getPackageVersion())
                .packageCreator(taskDO.getPackageCreator())
                .packageMd5(infoBO.getPackageMd5())
                .packagePath(infoBO.getStorageFile().toPath())
                .packageOptions(taskDO.getPackageOptions())
                .componentSchema(infoBO.getPackageMetaYaml())
                .build();
        taskDO.setPackagePath(infoBO.getStorageFile().toPath());
        taskDO.setPackageMd5(infoBO.getPackageMd5());
        taskDO.setTaskLog(infoBO.getLogContent());
        updateDatabaseRecord(componentPackageDO, taskDO);
        log.info("component package task has inserted to db||componentPackageTaskId={}||" +
                        "componentPackageId={}||appId={}||componentType={}||componentName={}||version={}||md5={}",
                taskDO.getId(), componentPackageDO.getId(), taskDO.getAppId(), taskDO.getComponentType(),
                taskDO.getComponentName(), taskDO.getPackageVersion(), infoBO.getPackageMd5());
        publisher.publishEvent(new SucceedComponentPackageTaskEvent(this, taskDO.getId()));
    }

    /**
     * ??????????????? Database ??????
     *
     * @param componentPackageDO Component Package ??????
     * @param taskDO             Component Package Task ??????
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateDatabaseRecord(ComponentPackageDO componentPackageDO, ComponentPackageTaskDO taskDO) {
        componentPackageRepository.insert(componentPackageDO);
        taskDO.setComponentPackageId(componentPackageDO.getId());
        componentPackageTaskRepository.updateByCondition(taskDO, ComponentPackageTaskQueryCondition.builder().id(taskDO.getId()).build());
    }
}
