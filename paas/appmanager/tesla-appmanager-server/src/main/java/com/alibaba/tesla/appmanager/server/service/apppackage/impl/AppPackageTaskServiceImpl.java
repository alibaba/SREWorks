package com.alibaba.tesla.appmanager.server.service.apppackage.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.VersionUtil;
import com.alibaba.tesla.appmanager.domain.req.componentpackage.ComponentPackageNextVersionReq;
import com.alibaba.tesla.appmanager.domain.req.componentpackage.ComponentPackageTaskNextVersionReq;
import com.alibaba.tesla.appmanager.domain.res.componentpackage.ComponentPackageNextVersionRes;
import com.alibaba.tesla.appmanager.domain.res.componentpackage.ComponentPackageTaskNextVersionRes;
import com.alibaba.tesla.appmanager.server.repository.AppPackageTagRepository;
import com.alibaba.tesla.appmanager.server.repository.AppPackageTaskRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppPackageTaskInQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.AppPackageTaskQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppPackageTagDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppPackageTaskDO;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageTaskService;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageService;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ?????????????????????
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class AppPackageTaskServiceImpl implements AppPackageTaskService {

    private final AppPackageTaskRepository appPackageTaskRepository;

    private final AppPackageTagRepository appPackageTagRepository;

    private final ComponentPackageService componentPackageService;

    private final ComponentPackageTaskService componentPackageTaskService;

    public AppPackageTaskServiceImpl(
            AppPackageTaskRepository appPackageTaskRepository, AppPackageTagRepository appPackageTagRepository,
            ComponentPackageService componentPackageService, ComponentPackageTaskService componentPackageTaskService) {
        this.appPackageTaskRepository = appPackageTaskRepository;
        this.appPackageTagRepository = appPackageTagRepository;
        this.componentPackageService = componentPackageService;
        this.componentPackageTaskService = componentPackageTaskService;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param condition ????????????
     * @return List
     */
    @Override
    public Pagination<AppPackageTaskDO> list(AppPackageTaskQueryCondition condition) {
        List<AppPackageTaskDO> taskList = appPackageTaskRepository.selectByCondition(condition);
        if (condition.isWithBlobs()) {
            taskList.forEach(task -> {
                if (StringUtils.isNotEmpty(task.getPackageOptions())) {
                    JSONObject options = JSONObject.parseObject(task.getPackageOptions());
                    task.setTags(JSONObject.parseArray(options.getString("tags"), String.class));
                }
            });
        }

        List<Long> appPackageIdList = taskList.stream()
                .filter(task -> Objects.nonNull(task.getAppPackageId()))
                .map(AppPackageTaskDO::getAppPackageId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(appPackageIdList)) {
            List<AppPackageTagDO> onSaleList = appPackageTagRepository.query(appPackageIdList,
                    DefaultConstant.ON_SALE);

            if (CollectionUtils.isNotEmpty(onSaleList)) {
                List<Long> onSaleIdList = onSaleList.stream()
                        .map(AppPackageTagDO::getAppPackageId)
                        .collect(Collectors.toList());
                taskList.stream()
                        .filter(task -> onSaleIdList.contains(task.getAppPackageId()))
                        .forEach(task -> task.setIsOnSale(Boolean.TRUE));
            }
        }
        return Pagination.valueOf(taskList, Function.identity());
    }

    /**
     * ?????? ID List ??????????????????????????? (??????????????? Blob ??????)
     *
     * @param condition ????????????
     * @return List
     */
    @Override
    public List<AppPackageTaskDO> listIn(AppPackageTaskInQueryCondition condition) {
        return appPackageTaskRepository.selectByCondition(AppPackageTaskQueryCondition.builder()
                .idList(condition.getIdList())
                .withBlobs(false)
                .build());
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param condition ????????????
     * @return ????????????
     */
    @Override
    public AppPackageTaskDO get(AppPackageTaskQueryCondition condition) {
        Pagination<AppPackageTaskDO> results = list(condition);
        if (results.isEmpty()) {
            return null;
        } else if (results.getTotal() > 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("multiple app package tasks found, abort|condition=%s",
                            JSONObject.toJSONString(condition)));
        }
        return results.getItems().get(0);
    }

    @Override
    public int delete(AppPackageTaskQueryCondition condition) {
        return appPackageTaskRepository.deleteByCondition(condition);
    }

    /**
     * ?????????????????????????????? Version
     *
     * @param appId         ?????? ID
     * @param componentType ????????????
     * @param componentName ????????????
     * @param fullVersion   ??????????????????????????? _?????????????????????
     * @return next version
     */
    @Override
    public String getComponentNextVersion(
            String appId, ComponentTypeEnum componentType, String componentName, String fullVersion) {
        if (!StringUtils.equals(fullVersion, DefaultConstant.AUTO_VERSION)) {
            return fullVersion;
        }

        // ??? version ??? _ ??????????????????????????????????????????????????????????????????????????? nextVersion
        ComponentPackageNextVersionRes packageNextVersion = componentPackageService.nextVersion(
                ComponentPackageNextVersionReq.builder()
                        .appId(appId)
                        .componentType(componentType.toString())
                        .componentName(componentName)
                        .build());
        ComponentPackageTaskNextVersionRes taskNextVersion = componentPackageTaskService.nextVersion(
                ComponentPackageTaskNextVersionReq.builder()
                        .appId(appId)
                        .componentType(componentType.toString())
                        .componentName(componentName)
                        .build());
        fullVersion = packageNextVersion.getNextVersion();
        if (VersionUtil.compareTo(fullVersion, taskNextVersion.getNextVersion()) < 0) {
            fullVersion = taskNextVersion.getNextVersion();
        }
        return fullVersion;
    }
}
