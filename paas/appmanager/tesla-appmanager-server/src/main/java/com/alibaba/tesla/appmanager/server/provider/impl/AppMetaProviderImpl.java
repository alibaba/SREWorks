package com.alibaba.tesla.appmanager.server.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.AppMetaProvider;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.AppOptionUpdateModeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.AppDeployEnvironmentDTO;
import com.alibaba.tesla.appmanager.domain.dto.AppMetaDTO;
import com.alibaba.tesla.appmanager.domain.req.AppMetaQueryReq;
import com.alibaba.tesla.appmanager.domain.req.AppMetaUpdateReq;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.condition.K8sMicroserviceMetaQueryCondition;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.service.K8sMicroserviceMetaService;
import com.alibaba.tesla.appmanager.server.assembly.AppMetaDtoConvert;
import com.alibaba.tesla.appmanager.server.repository.condition.*;
import com.alibaba.tesla.appmanager.server.repository.domain.AppAddonDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppMetaDO;
import com.alibaba.tesla.appmanager.server.service.appaddon.AppAddonService;
import com.alibaba.tesla.appmanager.server.service.appmeta.AppMetaService;
import com.alibaba.tesla.appmanager.server.service.appoption.AppOptionService;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageService;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageTagService;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageTaskService;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageService;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageTaskService;
import com.alibaba.tesla.appmanager.server.service.rtappinstance.RtAppInstanceService;
import com.google.common.base.Enums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 应用元信息接口
 *
 * @author qianmo.zm@alibaba-inc.com
 */
@Slf4j
@Service
public class AppMetaProviderImpl implements AppMetaProvider {

    @Autowired
    private AppMetaService appMetaService;

    @Autowired
    private AppMetaDtoConvert appMetaDtoConvert;

    @Autowired
    private AppAddonService appAddonService;

    @Autowired
    private K8sMicroserviceMetaService k8sMicroserviceMetaService;

    @Autowired
    private AppPackageTagService appPackageTagService;

    @Autowired
    private AppPackageService appPackageService;

    @Autowired
    private AppPackageTaskService appPackageTaskService;

    @Autowired
    private ComponentPackageTaskService componentPackageTaskService;

    @Autowired
    private ComponentPackageService componentPackageService;

    @Autowired
    private AppOptionService appOptionService;

    @Autowired
    private RtAppInstanceService rtAppInstanceService;

    /**
     * 分页查询应用元信息
     */
    @Override
    public Pagination<AppMetaDTO> list(AppMetaQueryReq request) {
        AppMetaQueryCondition condition = AppMetaQueryCondition.builder()
                .appId(request.getAppId())
                .optionKey(request.getOptionKey())
                .optionValue(request.getOptionValue())
                .page(request.getPage())
                .pageSize(request.getPageSize())
                .build();
        Pagination<AppMetaDO> metaList = appMetaService.list(condition);
        return Pagination.transform(metaList, item -> {
            AppMetaDTO dto = appMetaDtoConvert.to(item);
            dto.setOptions(appOptionService.getOptionMap(item.getAppId()));
            dto.setEnvironments(rtAppInstanceService.list(
                            RtAppInstanceQueryCondition.builder()
                                    .appId(item.getAppId())
                                    .pageSize(DefaultConstant.UNLIMITED_PAGE_SIZE)
                                    .build())
                    .getItems()
                    .stream()
                    .map(instance -> AppDeployEnvironmentDTO.builder()
                            .clusterId(instance.getClusterId())
                            .namespaceId(instance.getNamespaceId())
                            .stageId(instance.getStageId())
                            .build())
                    .collect(Collectors.toList()));
            return dto;
        });
    }

    /**
     * 通过应用 ID 查询应用元信息
     */
    @Override
    public AppMetaDTO get(String appId) {
        Pagination<AppMetaDTO> results = list(AppMetaQueryReq.builder().appId(appId).build());
        if (results.isEmpty()) {
            return null;
        } else if (results.getTotal() > 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "multiple app found with appId " + appId);
        }
        return results.getItems().get(0);
    }

    /**
     * 通过应用 ID 删除应用元信息
     */
    @Override
    public boolean delete(String appId) {
        if (StringUtils.isEmpty(appId)) {
            return true;
        }

        deleteAppMeta(appId);
        log.info("action=appMetaProvider|deleteAppMeta SUCCESS|appId={}", appId);

        deleteAppAddon(appId);
        log.info("action=appMetaProvider|deleteAppAddon SUCCESS|appId={}", appId);

        deleteK8sMicroServiceMeta(appId);
        log.info("action=appMetaProvider|deleteK8sMicroServiceMeta SUCCESS|appId={}", appId);

        deleteAppPackageTag(appId);
        log.info("action=appMetaProvider|deleteAppPackageTag SUCCESS|appId={}", appId);

        deleteComponentPackageTask(appId);
        log.info("action=appMetaProvider|deleteComponentPackageTask SUCCESS|appId={}", appId);

        deleteComponentPackage(appId);
        log.info("action=appMetaProvider|deleteComponentPackage SUCCESS|appId={}", appId);

        deleteAppPackageTask(appId);
        log.info("action=appMetaProvider|deleteAppPackageTask SUCCESS|appId={}", appId);

        deleteAppPackage(appId);
        log.info("action=appMetaProvider|deleteAppPackage SUCCESS|appId={}", appId);

        return true;
    }

    private void deleteAppMeta(String appId) {
        AppMetaQueryCondition condition = AppMetaQueryCondition.builder().appId(appId).build();
        appMetaService.delete(condition);
        appOptionService.deleteOptions(appId);
    }

    private void deleteAppAddon(String appId) {
        AppAddonQueryCondition condition = AppAddonQueryCondition.builder().appId(appId).build();
        Pagination<AppAddonDO> records = appAddonService.list(condition);
        records.getItems().forEach(item -> appAddonService.delete(AppAddonQueryCondition.builder()
                .appId(item.getAppId())
                .addonTypeList(Collections.singletonList(item.getAddonType()))
                .addonId(item.getAddonId())
                .addonName(item.getName())
                .build()));
    }

    private void deleteK8sMicroServiceMeta(String appId) {
        K8sMicroserviceMetaQueryCondition condition = K8sMicroserviceMetaQueryCondition.builder().appId(appId).build();
        k8sMicroserviceMetaService.delete(condition);
    }

    private void deleteComponentPackageTask(String appId) {
        ComponentPackageTaskQueryCondition condition = ComponentPackageTaskQueryCondition.builder().appId(appId)
                .build();
        componentPackageTaskService.delete(condition);
    }

    private void deleteAppPackage(String appId) {
        AppPackageQueryCondition condition = AppPackageQueryCondition.builder().appId(appId).build();
        appPackageService.delete(condition);
    }

    private void deleteAppPackageTask(String appId) {
        AppPackageTaskQueryCondition condition = AppPackageTaskQueryCondition.builder().appId(appId).build();
        appPackageTaskService.delete(condition);
    }

    private void deleteComponentPackage(String appId) {
        ComponentPackageQueryCondition condition = ComponentPackageQueryCondition.builder().appId(appId).build();
        componentPackageService.delete(condition);
    }

    private void deleteAppPackageTag(String appId) {
        AppPackageTagQueryCondition condition = AppPackageTagQueryCondition.builder().appId(appId).build();
        appPackageTagService.delete(condition);
    }

    /**
     * 保存应用元信息
     */
    @Override
    public AppMetaDTO save(AppMetaUpdateReq request) {
        String appId = request.getAppId();
        JSONObject options = request.getOptions();

        // 如果不存在就新增，如果存在就不用动了
        AppMetaQueryCondition condition = AppMetaQueryCondition.builder().appId(appId).build();
        if (appMetaService.get(condition) == null) {
            appMetaService.create(AppMetaDO.builder().appId(appId).build());
        }

        // 更新 Options
        AppOptionUpdateModeEnum mode = Enums.getIfPresent(AppOptionUpdateModeEnum.class,
                request.getMode().toUpperCase()).orNull();
        if (mode == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "invalid parameter mode " + request.getMode());
        }
        appOptionService.updateOptions(appId, options, mode);
        return get(appId);
    }
}
