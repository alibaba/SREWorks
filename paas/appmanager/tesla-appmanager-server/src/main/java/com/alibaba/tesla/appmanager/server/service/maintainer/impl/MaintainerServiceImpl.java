package com.alibaba.tesla.appmanager.server.service.maintainer.impl;

import com.alibaba.tesla.appmanager.deployconfig.repository.DeployConfigRepository;
import com.alibaba.tesla.appmanager.deployconfig.repository.condition.DeployConfigQueryCondition;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigEnvId;
import com.alibaba.tesla.appmanager.meta.helm.repository.HelmMetaRepository;
import com.alibaba.tesla.appmanager.meta.helm.repository.condition.HelmMetaQueryCondition;
import com.alibaba.tesla.appmanager.meta.helm.repository.domain.HelmMetaDO;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.K8sMicroServiceMetaRepository;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.condition.K8sMicroserviceMetaQueryCondition;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.domain.K8sMicroServiceMetaDO;
import com.alibaba.tesla.appmanager.server.repository.AppAddonRepository;
import com.alibaba.tesla.appmanager.server.repository.ComponentPackageTaskRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppAddonQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.ComponentPackageTaskQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppAddonDO;
import com.alibaba.tesla.appmanager.server.repository.domain.ComponentPackageTaskDO;
import com.alibaba.tesla.appmanager.server.service.maintainer.MaintainerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统维护 Service
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class MaintainerServiceImpl implements MaintainerService {

    @Autowired
    private AppAddonRepository appAddonRepository;

    @Autowired
    private DeployConfigRepository deployConfigRepository;

    @Autowired
    private ComponentPackageTaskRepository componentPackageTaskRepository;

    @Autowired
    private K8sMicroServiceMetaRepository k8sMicroServiceMetaRepository;

    @Autowired
    private HelmMetaRepository helmMetaRepository;

    /**
     * 升级 namespaceId / stageId (针对各 meta 表新增的 namespaceId / stageId 空字段进行初始化)
     *
     * @param namespaceId Namespace ID
     * @param stageId     Stage ID
     */
    @Override
    public void upgradeNamespaceStage(String namespaceId, String stageId) {
        upgradeNamespaceStageForAppAddon(namespaceId, stageId);
        upgradeNamespaceStageForDeployConfig(namespaceId, stageId);
        upgradeNamespaceStageForComponentPackageTasks(namespaceId, stageId);
        upgradeNamespaceStageForK8sMicroServices(namespaceId, stageId);
        upgradeNamespaceStageForHelm(namespaceId, stageId);
    }

    /**
     * 升级 helm 中的 namespaceId / stageId
     *
     * @param namespaceId Namespace ID
     * @param stageId     Stage ID
     */
    private void upgradeNamespaceStageForHelm(String namespaceId, String stageId) {
        HelmMetaQueryCondition condition = HelmMetaQueryCondition.builder()
                .namespaceId("")
                .stageId("")
                .build();
        List<HelmMetaDO> records = helmMetaRepository.selectByCondition(condition);
        for (HelmMetaDO record : records) {
            record.setNamespaceId(namespaceId);
            record.setStageId(stageId);
            helmMetaRepository.updateByCondition(record,
                    HelmMetaQueryCondition.builder().id(record.getId()).build());
            log.info("upgrade namespace and stage field in helm record|appId={}|namespaceId={}|" +
                            "stageId={}|componentType={}|name={}|helmPackageId={}", record.getAppId(),
                    namespaceId, stageId, record.getComponentType(), record.getName(), record.getHelmPackageId());
        }
    }

    /**
     * 升级 k8s microservices 中的 namespaceId / stageId
     *
     * @param namespaceId Namespace ID
     * @param stageId     Stage ID
     */
    private void upgradeNamespaceStageForK8sMicroServices(String namespaceId, String stageId) {
        K8sMicroserviceMetaQueryCondition condition = K8sMicroserviceMetaQueryCondition.builder()
                .namespaceId("")
                .stageId("")
                .build();
        List<K8sMicroServiceMetaDO> records = k8sMicroServiceMetaRepository.selectByCondition(condition);
        for (K8sMicroServiceMetaDO record : records) {
            record.setNamespaceId(namespaceId);
            record.setStageId(stageId);
            k8sMicroServiceMetaRepository.updateByCondition(record,
                    K8sMicroserviceMetaQueryCondition.builder().id(record.getId()).build());
            log.info("upgrade namespace and stage field in k8s microservice record|appId={}|namespaceId={}|" +
                            "stageId={}|componentType={}|microserviceId={}", record.getAppId(),
                    namespaceId, stageId, record.getComponentType(), record.getMicroServiceId());
        }
    }

    /**
     * 升级 component package tasks 中的 namespaceId / stageId
     *
     * @param namespaceId Namespace ID
     * @param stageId     Stage ID
     */
    private void upgradeNamespaceStageForComponentPackageTasks(String namespaceId, String stageId) {
        ComponentPackageTaskQueryCondition condition = ComponentPackageTaskQueryCondition.builder()
                .namespaceId("")
                .stageId("")
                .build();
        List<ComponentPackageTaskDO> records = componentPackageTaskRepository.selectByCondition(condition);
        for (ComponentPackageTaskDO record : records) {
            record.setNamespaceId(namespaceId);
            record.setStageId(stageId);
            componentPackageTaskRepository.updateByCondition(record,
                    ComponentPackageTaskQueryCondition.builder().id(record.getId()).build());
            log.info("upgrade namespace and stage field in component package task record|appId={}|namespaceId={}|" +
                            "stageId={}|componentType={}|componentName={}|packageVersion={}", record.getAppId(),
                    namespaceId, stageId, record.getComponentType(), record.getComponentName(),
                    record.getPackageVersion());
        }
    }

    /**
     * 升级 deploy config 中的 namespaceId / stageId
     *
     * @param namespaceId Namespace ID
     * @param stageId     Stage ID
     */
    private void upgradeNamespaceStageForDeployConfig(String namespaceId, String stageId) {
        DeployConfigQueryCondition condition = DeployConfigQueryCondition.builder()
                .envId("")
                .build();
        List<DeployConfigDO> records = deployConfigRepository.selectByExample(condition);
        for (DeployConfigDO record : records) {
            record.setEnvId(DeployConfigEnvId.namespaceStageStr(namespaceId, stageId));
            deployConfigRepository.updateByExampleSelective(record,
                    DeployConfigQueryCondition.builder().id(record.getId()).build());
            log.info("upgrade namespace and stage field in deploy config record|appId={}|typeId={}|envId={}|" +
                    "inherit={}", record.getAppId(), record.getTypeId(), record.getEnabled(), record.getInherit());
        }
    }

    /**
     * 升级 app addon 中的 namespaceId / stageId
     *
     * @param namespaceId Namespace ID
     * @param stageId     Stage ID
     */
    private void upgradeNamespaceStageForAppAddon(String namespaceId, String stageId) {
        AppAddonQueryCondition condition = AppAddonQueryCondition.builder()
                .namespaceId("")
                .stageId("")
                .build();
        List<AppAddonDO> records = appAddonRepository.selectByCondition(condition);
        for (AppAddonDO record : records) {
            record.setNamespaceId(namespaceId);
            record.setStageId(stageId);
            int count = appAddonRepository
                    .updateByCondition(record, AppAddonQueryCondition.builder().id(record.getId()).build());
            if (count > 0) {
                log.info("upgrade namespace and stage field in app addon record|appId={}|namespaceId={}|stageId={}|" +
                                "appAddonId={}", record.getAppId(), record.getNamespaceId(), record.getStageId(),
                        record.getId());
            } else {
                log.error("upgrade namespace and stage field failed, count=0|appId={}|namespaceId={}|stageId={}|" +
                                "appAddonId={}", record.getAppId(), record.getNamespaceId(), record.getStageId(),
                        record.getId());
            }
        }
    }
}
