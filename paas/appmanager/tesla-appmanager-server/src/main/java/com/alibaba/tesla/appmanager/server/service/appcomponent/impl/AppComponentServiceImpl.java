package com.alibaba.tesla.appmanager.server.service.appcomponent.impl;

import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.container.AppComponentLocationContainer;
import com.alibaba.tesla.appmanager.meta.helm.repository.condition.HelmMetaQueryCondition;
import com.alibaba.tesla.appmanager.meta.helm.repository.domain.HelmMetaDO;
import com.alibaba.tesla.appmanager.meta.helm.service.HelmMetaService;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.condition.K8sMicroserviceMetaQueryCondition;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.domain.K8sMicroServiceMetaDO;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.service.K8sMicroserviceMetaService;
import com.alibaba.tesla.appmanager.server.repository.AppComponentRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppAddonQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.AppComponentQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppAddonDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDO;
import com.alibaba.tesla.appmanager.server.service.appaddon.AppAddonService;
import com.alibaba.tesla.appmanager.server.service.appcomponent.AppComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 应用绑定组件服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class AppComponentServiceImpl implements AppComponentService {

    @Autowired
    private AppAddonService appAddonService;

    @Autowired
    private AppComponentRepository appComponentRepository;

    @Autowired
    private K8sMicroserviceMetaService k8sMicroserviceMetaService;

    @Autowired
    private HelmMetaService helmMetaService;

    /**
     * 获取指定应用下绑定了哪些组件及组件名称
     * @param appId 应用 ID
     * @param isolateNamespaceId 隔离 Namespace ID
     * @param isolateStageId 隔离 Stage ID
     * @return List of AppComponentLocationContainer
     */
    @Override
    public List<AppComponentLocationContainer> getFullComponentRelations(
            String appId, String isolateNamespaceId, String isolateStageId) {
        List<AppComponentDO> appComponents = list(AppComponentQueryCondition.builder()
                .appId(appId)
                .namespaceId(isolateNamespaceId)
                .stageId(isolateStageId)
                .build());
        Pagination<K8sMicroServiceMetaDO> appMicroservices = k8sMicroserviceMetaService.list(
                K8sMicroserviceMetaQueryCondition.builder()
                        .appId(appId)
                        .namespaceId(isolateNamespaceId)
                        .stageId(isolateStageId)
                        .build());
        Pagination<HelmMetaDO> appHelms = helmMetaService.list(HelmMetaQueryCondition.builder()
                .appId(appId)
                .namespaceId(isolateNamespaceId)
                .stageId(isolateStageId)
                .build());
        Pagination<AppAddonDO> appAddons = appAddonService.list(AppAddonQueryCondition.builder()
                .appId(appId)
                .namespaceId(isolateNamespaceId)
                .stageId(isolateStageId)
                .build());
        List<AppComponentLocationContainer> containers = new ArrayList<>();
        Set<String> usedSet = new HashSet<>();
        appComponents.forEach(item -> {
            String key = String.format("%s_%s", item.getComponentType(), item.getComponentName());
            if (!usedSet.contains(key)) {
                containers.add(AppComponentLocationContainer.builder()
                        .componentType(item.getComponentType())
                        .componentName(item.getComponentName())
                        .build());
            }
            usedSet.add(key);
        });
        appMicroservices.getItems().forEach(item -> {
            String key = String.format("%s_%s", item.getComponentType(), item.getMicroServiceId());
            if (!usedSet.contains(key)) {
                containers.add(AppComponentLocationContainer.builder()
                        .componentType(item.getComponentType())
                        .componentName(item.getMicroServiceId())
                        .build());
            }
            usedSet.add(key);
        });
        appHelms.getItems().forEach(item -> {
            String key = String.format("%s_%s", item.getComponentType(), item.getHelmPackageId());
            if (!usedSet.contains(key)) {
                containers.add(AppComponentLocationContainer.builder()
                        .componentType(item.getComponentType())
                        .componentName(item.getHelmPackageId())
                        .build());
            }
            usedSet.add(key);
        });
        appAddons.getItems().forEach(item -> {
            String key = String.format("%s_%s", item.getAddonType(), item.getAddonId());
            if (!usedSet.contains(key)) {
                containers.add(AppComponentLocationContainer.builder()
                        .componentType(item.getAddonType())
                        .componentName(item.getAddonId())
                        .build());
            }
        });
        return containers;
    }

    /**
     * 根据条件过滤应用绑定组件
     *
     * @param condition 过滤条件
     * @return List
     */
    @Override
    public List<AppComponentDO> list(AppComponentQueryCondition condition) {
        return appComponentRepository.selectByCondition(condition);
    }

    /**
     * 根据条件获取指定的应用绑定组件
     *
     * @param condition 查询条件
     * @return List
     */
    @Override
    public AppComponentDO get(AppComponentQueryCondition condition) {
        condition.setWithBlobs(true);
        List<AppComponentDO> records = appComponentRepository.selectByCondition(condition);
        if (records.size() == 0) {
            return null;
        } else {
            return records.get(0);
        }
    }

    /**
     * 创建应用绑定组件
     *
     * @param record 绑定记录
     * @return 数据库更新数量
     */
    @Override
    public int create(AppComponentDO record) {
        return appComponentRepository.insert(record);
    }

    /**
     * 更新应用绑定组件
     *
     * @param record    绑定记录
     * @param condition 查询条件
     * @return 数据库更新数量
     */
    @Override
    public int update(AppComponentDO record, AppComponentQueryCondition condition) {
        return appComponentRepository.updateByCondition(record, condition);
    }

    /**
     * 删除应用绑定组件
     *
     * @param condition 查询条件
     * @return 数据库更新数量
     */
    @Override
    public int delete(AppComponentQueryCondition condition) {
        return appComponentRepository.deleteByCondition(condition);
    }
}
