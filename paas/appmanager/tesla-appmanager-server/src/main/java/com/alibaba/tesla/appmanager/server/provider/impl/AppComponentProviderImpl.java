package com.alibaba.tesla.appmanager.server.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.AppAddonProvider;
import com.alibaba.tesla.appmanager.api.provider.AppComponentProvider;
import com.alibaba.tesla.appmanager.api.provider.HelmMetaProvider;
import com.alibaba.tesla.appmanager.api.provider.K8sMicroServiceMetaProvider;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.PluginKindEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.ClassUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.deployconfig.service.DeployConfigService;
import com.alibaba.tesla.appmanager.deployconfig.util.DeployConfigGenerator;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigTypeId;
import com.alibaba.tesla.appmanager.domain.dto.AppComponentDTO;
import com.alibaba.tesla.appmanager.domain.req.AppAddonQueryReq;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaQueryReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentCreateReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentDeleteReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentQueryReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentUpdateReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigUpsertReq;
import com.alibaba.tesla.appmanager.domain.req.helm.HelmMetaQueryReq;
import com.alibaba.tesla.appmanager.plugin.repository.condition.PluginDefinitionQueryCondition;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import com.alibaba.tesla.appmanager.plugin.service.PluginService;
import com.alibaba.tesla.appmanager.server.assembly.AppComponentDtoConvert;
import com.alibaba.tesla.appmanager.server.repository.condition.AppComponentQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDO;
import com.alibaba.tesla.appmanager.server.service.appcomponent.AppComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用关联组件 Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class AppComponentProviderImpl implements AppComponentProvider {

    @Autowired
    private AppComponentService appComponentService;

    @Autowired
    private AppComponentDtoConvert appComponentDtoConvert;

    @Autowired
    private K8sMicroServiceMetaProvider k8SMicroServiceMetaProvider;

    @Autowired
    private AppAddonProvider appAddonProvider;

    @Autowired
    private HelmMetaProvider helmMetaProvider;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private DeployConfigService deployConfigService;

    /**
     * 获取指定应用下的指定关联 Component 对象
     *
     * @param request  应用组件绑定查询请求
     * @param operator 操作人
     * @return AppComponentDTO
     */
    @Override
    public AppComponentDTO get(AppComponentQueryReq request, String operator) {
        AppComponentQueryCondition condition = new AppComponentQueryCondition();
        ClassUtil.copy(request, condition);
        return appComponentDtoConvert.to(appComponentService.get(condition));
    }

    /**
     * 创建应用下的关联 Component 绑定
     *
     * @param request  创建请求
     * @param operator 操作人
     * @return 绑定后的结果
     */
    @Override
    public AppComponentDTO create(AppComponentCreateReq request, String operator) {
        String namespaceId = request.getNamespaceId();
        String stageId = request.getStageId();
        String appId = request.getAppId();
        String category = request.getCategory();
        String componentType = request.getComponentType();
        String componentName = request.getComponentName();
        String config = JSONObject.toJSONString(request.getConfig());

        // 提前检查是否已经存在记录
        AppComponentDO origin = appComponentService.get(AppComponentQueryCondition.builder()
                .namespaceId(namespaceId)
                .stageId(stageId)
                .appId(appId)
                .category(category)
                .componentType(componentType)
                .componentName(componentName)
                .build());
        if (origin != null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "the app component binding record exists");
        }

        AppComponentDO record = AppComponentDO.builder()
                .namespaceId(namespaceId)
                .stageId(stageId)
                .appId(appId)
                .category(category)
                .componentType(componentType)
                .componentName(componentName)
                .config(config)
                .build();
        int count = appComponentService.create(record);
        if (count != 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid count when creating app component|count=%d|record=%s",
                            count, JSONObject.toJSONString(record)));
        }
        log.info("app component has created|operator={}|id={}|namespaceId={}|stageId={}|appId={}|category={}|" +
                        "componentType={}|componentName={}|config={}", record.getId(), operator, namespaceId,
                stageId, appId, category, componentType, componentName, config);

        DeployConfigGenerator configObject = new DeployConfigGenerator();
        configObject.addRevisionName(componentType, componentName);
        configObject.addScope("Namespace", namespaceId);
        String typeId = new DeployConfigTypeId(componentType, componentName).toString();
        deployConfigService.update(DeployConfigUpsertReq.builder()
                .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                .appId(appId)
                .typeId(typeId)
                .envId("")
                .inherit(false)
                .config(configObject.toString())
                .isolateNamespaceId(namespaceId)
                .isolateStageId(stageId)
                .build());

        return appComponentDtoConvert.to(appComponentService.get(AppComponentQueryCondition.builder()
                .id(record.getId())
                .build()));
    }

    /**
     * 更新应用下的关联 Component 绑定
     *
     * @param request  更新请求
     * @param operator 操作人
     * @return 绑定后的结果
     */
    @Override
    public AppComponentDTO update(AppComponentUpdateReq request, String operator) {
        String namespaceId = request.getNamespaceId();
        String stageId = request.getStageId();
        String appId = request.getAppId();
        String category = request.getCategory();
        String componentType = request.getComponentType();
        String componentName = request.getComponentName();
        String config = JSONObject.toJSONString(request.getConfig());
        AppComponentQueryCondition condition = AppComponentQueryCondition.builder()
                .id(request.getId())
                .build();
        AppComponentDO record = appComponentService.get(condition);
        if (record == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "cannot find specified app component record");
        }

        record.setConfig(config);
        int count = appComponentService.update(record, condition);
        if (count != 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid count when updating app component|count=%d|record=%s",
                            count, JSONObject.toJSONString(record)));
        }
        log.info("app component has updated|operator={}|namespaceId={}|stageId={}|appId={}|category={}|" +
                        "componentType={}|componentName={}|config={}", operator, namespaceId, stageId, appId, category,
                componentType, componentName, config);

        DeployConfigGenerator configObject = new DeployConfigGenerator();
        configObject
                .addRevisionName(componentType, componentName)
                .addScope("Namespace", namespaceId)
                .addDataInputs(request.getConfig().getJSONArray("dataInputs"))
                .addDataOutputs(request.getConfig().getJSONArray("dataOutputs"));
        String typeId = new DeployConfigTypeId(componentType, componentName).toString();
        deployConfigService.update(DeployConfigUpsertReq.builder()
                .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                .appId(appId)
                .typeId(typeId)
                .envId("")
                .inherit(false)
                .config(configObject.toString())
                .isolateNamespaceId(namespaceId)
                .isolateStageId(stageId)
                .build());

        return appComponentDtoConvert.to(appComponentService.get(condition));
    }

    /**
     * 删除指定应用下的指定关联 Component 对象
     *
     * @param request  应用组件绑定查询请求
     * @param operator 操作人
     */
    @Override
    public void delete(AppComponentDeleteReq request, String operator) {
        AppComponentQueryCondition condition = AppComponentQueryCondition.builder().id(request.getId()).build();
        if (condition.getId() == null || condition.getId() == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "empty app component delete request");
        }
        appComponentService.delete(condition);
    }

    /**
     * 获取指定 appId 下的所有关联 Component 对象
     *
     * @param request  查询请求
     * @param operator 操作人
     * @return List of AppComponentDTO
     */
    @Override
    public List<AppComponentDTO> list(AppComponentQueryReq request, String operator) {
        String appId = request.getAppId();
        String namespaceId = request.getNamespaceId();
        String stageId = request.getStageId();
        String arch = request.getArch();
        boolean isWithBlobs = request.isWithBlobs();

        // 获取通用 Component
        List<AppComponentDO> appComponents = appComponentService.list(AppComponentQueryCondition.builder()
                .appId(appId)
                .namespaceId(namespaceId)
                .stageId(stageId)
                .withBlobs(isWithBlobs)
                .build());
        Map<String, PluginDefinitionDO> pluginMap = pluginService
                .list(PluginDefinitionQueryCondition.builder()
                        .pluginKind(PluginKindEnum.COMPONENT_DEFINITION.toString())
                        .pluginRegistered(true)
                        .build())
                .getItems()
                .stream()
                .collect(Collectors.toMap(PluginDefinitionDO::getPluginName, Function.identity()));
        List<AppComponentDTO> result = appComponents.stream()
                .filter(item -> pluginMap.containsKey(item.getComponentType()))
                .map(item -> appComponentDtoConvert.to(item, pluginMap.get(item.getComponentType())))
                .collect(Collectors.toList());
        log.info("fetch app component records|appId={}|namespaceId={}|stageId={}|size={}",
                appId, namespaceId, stageId, result.size());

        // 获取 K8S 微应用组件 TODO: 迁移到通用 Component
        K8sMicroServiceMetaQueryReq k8sMicroServiceMetaQueryReq = new K8sMicroServiceMetaQueryReq();
        k8sMicroServiceMetaQueryReq.setAppId(appId);
        k8sMicroServiceMetaQueryReq.setNamespaceId(namespaceId);
        k8sMicroServiceMetaQueryReq.setArch(arch);
        k8sMicroServiceMetaQueryReq.setStageId(stageId);
        k8sMicroServiceMetaQueryReq.setPagination(false);
        k8SMicroServiceMetaProvider.list(k8sMicroServiceMetaQueryReq).getItems()
                .forEach(k8sMicroServiceMetaDTO ->
                        result.add(AppComponentDTO.builder()
                                .id(k8sMicroServiceMetaDTO.getId())
                                .compatible(true)
                                .appId(appId)
                                .namespaceId(namespaceId)
                                .stageId(stageId)
                                .componentType(k8sMicroServiceMetaDTO.getComponentType())
                                .componentName(k8sMicroServiceMetaDTO.getMicroServiceId())
                                .build()
                        )
                );
        log.info("fetch k8s microservice records|appId={}|namespaceId={}|stageId={}|total={}",
                appId, namespaceId, stageId, result.size());

        // 获取 HELM 组件 TODO: 迁移到通用 Component
        HelmMetaQueryReq helmMetaQueryReq = new HelmMetaQueryReq();
        helmMetaQueryReq.setAppId(appId);
        helmMetaQueryReq.setNamespaceId(namespaceId);
        helmMetaQueryReq.setStageId(stageId);
        helmMetaQueryReq.setPagination(false);
        helmMetaProvider.list(helmMetaQueryReq).getItems()
                .forEach(helmMetaDO ->
                        result.add(AppComponentDTO.builder()
                                .id(helmMetaDO.getId())
                                .compatible(true)
                                .appId(appId)
                                .namespaceId(namespaceId)
                                .stageId(stageId)
                                .componentName(helmMetaDO.getHelmPackageId())
                                .componentType(helmMetaDO.getComponentType())
                                .build()
                        )

                );
        log.info("fetch helm microservice records|appId={}|namespaceId={}|stageId={}|total={}",
                appId, namespaceId, stageId, result.size());

        // 获取 Internal Addon TODO: 迁移到通用 Component
        AppAddonQueryReq internalAddonQueryReq = new AppAddonQueryReq();
        internalAddonQueryReq.setAppId(appId);
        internalAddonQueryReq.setNamespaceId(namespaceId);
        internalAddonQueryReq.setStageId(stageId);
        internalAddonQueryReq.setPagination(false);
        internalAddonQueryReq.setAddonTypeList(Collections.singletonList(ComponentTypeEnum.INTERNAL_ADDON.toString()));
        appAddonProvider.list(internalAddonQueryReq).getItems()
                .forEach(item ->
                        result.add(AppComponentDTO.builder()
                                .id(item.getId())
                                .compatible(true)
                                .appId(appId)
                                .namespaceId(namespaceId)
                                .stageId(stageId)
                                .componentType(item.getAddonType())
                                .componentName(item.getAddonId())
                                .build()
                        )
                );
        log.info("fetch internal addon records|appId={}|namespaceId={}|stageId={}|total={}",
                appId, namespaceId, stageId, result.size());

        // 获取 Resource Addon TODO: 迁移到通用 Component
        AppAddonQueryReq resourceAddonQueryReq = new AppAddonQueryReq();
        resourceAddonQueryReq.setAppId(appId);
        resourceAddonQueryReq.setNamespaceId(namespaceId);
        resourceAddonQueryReq.setStageId(stageId);
        resourceAddonQueryReq.setPagination(false);
        resourceAddonQueryReq.setAddonTypeList(Collections.singletonList(ComponentTypeEnum.RESOURCE_ADDON.toString()));
        appAddonProvider.list(resourceAddonQueryReq).getItems()
                .forEach(item ->
                        result.add(AppComponentDTO.builder()
                                .id(item.getId())
                                .compatible(true)
                                .appId(appId)
                                .namespaceId(namespaceId)
                                .stageId(stageId)
                                .componentType(item.getAddonType())
                                .componentName(String.format("%s@%s", item.getAddonId(), item.getName()))
                                .build()
                        )
                );
        log.info("fetch resource addon records|appId={}|namespaceId={}|stageId={}|total={}",
                appId, namespaceId, stageId, result.size());
        return result;
    }
}
