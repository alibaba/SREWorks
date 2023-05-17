package com.alibaba.tesla.appmanager.server.service.apppackage.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.autoconfig.SystemProperties;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.constants.PackAppPackageVariableKey;
import com.alibaba.tesla.appmanager.common.enums.AppPackageTaskStatusEnum;
import com.alibaba.tesla.appmanager.common.enums.ComponentPackageTaskStateEnum;
import com.alibaba.tesla.appmanager.common.enums.DagTypeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.VersionUtil;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageTaskCreateReq;
import com.alibaba.tesla.appmanager.domain.req.apppackage.ComponentBinder;
import com.alibaba.tesla.appmanager.domain.req.componentpackage.ComponentPackageNextVersionReq;
import com.alibaba.tesla.appmanager.domain.req.componentpackage.ComponentPackageTaskNextVersionReq;
import com.alibaba.tesla.appmanager.domain.res.componentpackage.ComponentPackageNextVersionRes;
import com.alibaba.tesla.appmanager.domain.res.componentpackage.ComponentPackageTaskNextVersionRes;
import com.alibaba.tesla.appmanager.server.repository.*;
import com.alibaba.tesla.appmanager.server.repository.condition.AppPackageQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.AppPackageTaskInQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.AppPackageTaskQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.ComponentPackageTaskQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.*;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageService;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageTagService;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageTaskService;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageService;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageTaskService;
import com.alibaba.tesla.appmanager.server.service.pack.dag.PackAppPackageToStorageDag;
import com.alibaba.tesla.dag.services.DagInstService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用包任务服务
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

    private final AppPackageRepository appPackageRepository;

    private final ComponentPackageTaskRepository componentPackageTaskRepository;

    private final AppPackageComponentRelRepository appPackageComponentRelRepository;

    private final AppPackageTagService appPackageTagService;

    private final DagInstService dagInstService;

    private final AppPackageService appPackageService;

    private final SystemProperties systemProperties;

    private static final String ERROR_PREX = "error:";

    public AppPackageTaskServiceImpl(
            AppPackageTaskRepository appPackageTaskRepository, AppPackageTagRepository appPackageTagRepository,
            ComponentPackageService componentPackageService, ComponentPackageTaskService componentPackageTaskService,
            AppPackageRepository appPackageRepository, ComponentPackageTaskRepository componentPackageTaskRepository,
            AppPackageComponentRelRepository appPackageComponentRelRepository,
            AppPackageTagService appPackageTagService, DagInstService dagInstService,
            AppPackageService appPackageService, SystemProperties systemProperties) {
        this.appPackageTaskRepository = appPackageTaskRepository;
        this.appPackageTagRepository = appPackageTagRepository;
        this.componentPackageService = componentPackageService;
        this.componentPackageTaskService = componentPackageTaskService;
        this.appPackageRepository = appPackageRepository;
        this.componentPackageTaskRepository = componentPackageTaskRepository;
        this.appPackageComponentRelRepository = appPackageComponentRelRepository;
        this.appPackageTagService = appPackageTagService;
        this.dagInstService = dagInstService;
        this.appPackageService = appPackageService;
        this.systemProperties = systemProperties;
    }

    /**
     * 根据条件过滤应用包任务列表
     *
     * @param condition 过滤条件
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
     * 根据 ID List 列出应用包任务列表 (仅状态，无 Blob 数据)
     *
     * @param condition 过滤条件
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
     * 根据条件获取指定的应用包任务
     *
     * @param condition 过滤条件
     * @return 单个对象
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
     * 获取指定组件的下一个 Version
     *
     * @param appId         应用 ID
     * @param componentType 组件类型
     * @param componentName 组件名称
     * @param fullVersion   当前提供版本（可为 _，为自动生成）
     * @return next version
     */
    @Override
    public String getComponentNextVersion(
            String appId, String componentType, String componentName, String fullVersion) {
        if (!StringUtils.equals(fullVersion, DefaultConstant.AUTO_VERSION)) {
            return fullVersion;
        }

        // 当 version 为 _ 时（自动生成），根据当前组件包的版本号情况自动获取 nextVersion
        ComponentPackageNextVersionRes packageNextVersion = componentPackageService.nextVersion(
                ComponentPackageNextVersionReq.builder()
                        .appId(appId)
                        .componentType(componentType)
                        .componentName(componentName)
                        .build());
        ComponentPackageTaskNextVersionRes taskNextVersion = componentPackageTaskService.nextVersion(
                ComponentPackageTaskNextVersionReq.builder()
                        .appId(appId)
                        .componentType(componentType)
                        .componentName(componentName)
                        .build());
        fullVersion = packageNextVersion.getNextVersion();
        if (VersionUtil.compareTo(fullVersion, taskNextVersion.getNextVersion()) < 0) {
            fullVersion = taskNextVersion.getNextVersion();
        }
        return fullVersion;
    }

    /**
     * 更新指定的组件包任务对象到指定状态
     *
     * @param taskDO 组件包任务对象
     * @param state  目标指定状态
     */
    @Override
    public void updateComponentTaskStatus(ComponentPackageTaskDO taskDO, ComponentPackageTaskStateEnum state) {
        String oldStautus = taskDO.getTaskStatus();
        // 状态转移
        taskDO.setTaskStatus(state.toString());
        componentPackageTaskRepository.updateByCondition(taskDO,
                ComponentPackageTaskQueryCondition.builder().id(taskDO.getId()).build());
        log.info("actionName=RunningComponentPackageTask|status transitioned from {} to {}", oldStautus, state);
        // 刷新上层任务状态
        AppPackageTaskDO appPackageTask = get(AppPackageTaskQueryCondition.builder()
                .id(taskDO.getAppPackageTaskId())
                .withBlobs(true)
                .build());
        if (appPackageTask == null) {
            log.error("cannot find app package task records when updates component task status|" +
                    "componentPackageTaskId={}|appPackageTaskId={}", taskDO.getId(), taskDO.getAppPackageTaskId());
            return;
        }
        freshAppPackageTask(appPackageTask);
    }

    /**
     * 刷新指定的应用包任务
     *
     * @param appPackageTaskDO 应用包任务
     */
    @Override
    public void freshAppPackageTask(AppPackageTaskDO appPackageTaskDO) {
        ComponentPackageTaskQueryCondition condition = ComponentPackageTaskQueryCondition.builder()
                .appPackageTaskId(appPackageTaskDO.getId())
                .build();
        Pagination<ComponentPackageTaskDO> tasks = componentPackageTaskService.list(condition);
        if (tasks.isEmpty()) {
            log.debug("action=appPackageFreshJob|freshAppPackageTask|cannot find available component package tasks|" +
                    "appPackageTaskId={}", appPackageTaskDO.getId());
            return;
        }

        if (Objects.nonNull(appPackageTaskDO.getAppPackageId())) {
            AppPackageDO appPackageDO = appPackageRepository.getByCondition(
                    AppPackageQueryCondition.builder()
                            .id(appPackageTaskDO.getAppPackageId())
                            .withBlobs(true)
                            .build());

            if (StringUtils.isNotEmpty(appPackageDO.getPackagePath())) {
                if (StringUtils.startsWith(appPackageDO.getPackagePath(), ERROR_PREX)) {
                    appPackageTaskDO.setTaskStatus(AppPackageTaskStatusEnum.FAILURE.toString());
                } else {
                    appPackageTaskDO.setTaskStatus(AppPackageTaskStatusEnum.SUCCESS.toString());
                }
                appPackageTaskRepository.updateByCondition(
                        appPackageTaskDO,
                        AppPackageTaskQueryCondition.builder().id(appPackageTaskDO.getId()).build());
            }

            log.info("action=appPackageFreshJob|freshAppPackageTask|app package exists|appPackageId={}|packagePath={}",
                    appPackageDO.getId(), appPackageDO.getPackagePath());
            return;
        }

        String packageOptions = appPackageTaskDO.getPackageOptions();
        AppPackageTaskCreateReq appPackageTaskCreateReq = JSONObject.parseObject(
                packageOptions, AppPackageTaskCreateReq.class);
        if (appPackageTaskCreateReq == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid package options when fresh app package tasks|taskId=%d",
                            appPackageTaskDO.getId()));
        }
        List<ComponentBinder> components = appPackageTaskCreateReq.getComponents();
        int componentCount = CollectionUtils.size(components);
        int unStartCount = 0;
        int runningCount = 0;
        int failedCount = 0;
        int successCount = 0;

        List<String> componentPackageIdStringList = new ArrayList<>();
        for (ComponentBinder component : components) {
            ComponentPackageTaskDO componentPackageTaskDO = tasks.getItems().stream()
                    .filter(taskDO -> isSame(taskDO, component))
                    .findAny()
                    .orElse(null);

            if (Objects.isNull(componentPackageTaskDO)) {
                unStartCount++;
            } else {
                componentPackageIdStringList.add(String.valueOf(componentPackageTaskDO.getComponentPackageId()));

                String taskStatus = componentPackageTaskDO.getTaskStatus();
                if (isRunning(taskStatus)) {
                    runningCount++;
                    continue;
                }

                if (isSuccess(taskStatus)) {
                    successCount++;
                    continue;
                }

                if (isFailed(taskStatus)) {
                    failedCount++;
                    continue;
                }
            }
        }

        log.debug("action=appPackageFreshJob|freshAppPackageTask|appPackageTaskId={}|componentCount={}|unStartCount={}|"
                        + "runningCount={}|failedCount={}|successCount={}", appPackageTaskDO.getId(), componentCount,
                unStartCount, runningCount, failedCount, successCount);

        AppPackageDO appPackageDO = null;
        // 全部成功
        if (Objects.equals(componentCount, successCount)) {
            appPackageDO = createAppPackage(appPackageTaskDO, tasks.getItems());
            appPackageTaskDO.setAppPackageId(appPackageDO.getId());
            appPackageTaskDO.setTaskStatus(AppPackageTaskStatusEnum.APP_PACK_RUN.toString());
            addTag(appPackageDO.getAppId(), appPackageDO.getId(), appPackageTaskCreateReq.getTags());
        } else if (runningCount > 0 || unStartCount > 0) {
            appPackageTaskDO.setTaskStatus(AppPackageTaskStatusEnum.COM_PACK_RUN.toString());
        } else if (failedCount > 0) {
            appPackageTaskDO.setTaskStatus(AppPackageTaskStatusEnum.FAILURE.toString());
        }

        appPackageTaskRepository.updateByCondition(
                appPackageTaskDO,
                AppPackageTaskQueryCondition.builder().id(appPackageTaskDO.getId()).build());

        if (Objects.nonNull(appPackageDO)) {
            // 触发生成应用包的流程
            JSONObject variables = new JSONObject();
            variables.put(DefaultConstant.DAG_TYPE, DagTypeEnum.PACK_APP_PACKAGE.toString());
            variables.put(PackAppPackageVariableKey.APP_PACKAGE_ID, appPackageTaskDO.getAppPackageId());
            variables.put(PackAppPackageVariableKey.COMPONENT_PACKAGE_ID_LIST,
                    String.join(",", componentPackageIdStringList));
            long dagInstId = 0L;
            try {
                dagInstId = dagInstService.start(PackAppPackageToStorageDag.name, variables, true);
            } catch (Exception e) {
                log.error(
                        "action=appPackageFreshJob|ERROR|start pack app package dag failed|appPackageId={}|variables={}|"
                                + "exception={}", appPackageDO.getId(), variables.toJSONString(),
                        ExceptionUtils.getStackTrace(e));
                appPackageDO.setPackagePath(ERROR_PREX + e.getMessage());
                appPackageRepository.updateByPrimaryKeySelective(appPackageDO);
            }
            log.info(
                    "action=appPackageFreshJob|start pack app package dag success|appId={}|version={}|" +
                            "componentPackageIdList={}|dagInstId={}", appPackageDO.getAppId(), appPackageDO.getPackageVersion(),
                    JSONArray.toJSONString(componentPackageIdStringList), dagInstId);
        }
    }

    private static boolean isSame(ComponentPackageTaskDO componentPackageTaskDO,
                                  ComponentBinder component) {
        return StringUtils.equals(componentPackageTaskDO.getComponentName(), component.getComponentName())
                && StringUtils.equals(componentPackageTaskDO.getComponentType(), component.getComponentType());
    }

    private static boolean isRunning(String taskStatus) {
        return StringUtils.equals(ComponentPackageTaskStateEnum.CREATED.toString(), taskStatus) || StringUtils.equals(
                ComponentPackageTaskStateEnum.RUNNING.toString(), taskStatus);
    }

    private static boolean isSuccess(String taskStatus) {
        return StringUtils.equals(ComponentPackageTaskStateEnum.SUCCESS.toString(), taskStatus) || StringUtils.equals(
                ComponentPackageTaskStateEnum.SKIP.toString(), taskStatus);
    }

    private static boolean isFailed(String taskStatus) {
        return StringUtils.equals(ComponentPackageTaskStateEnum.FAILURE.toString(), taskStatus);
    }

    private void addTag(String appId, Long appPackageId, List<String> tagList) {
        if (CollectionUtils.isNotEmpty(tagList)) {
            // 去重,避免主键冲突
            tagList = tagList.stream().distinct().collect(Collectors.toList());
            tagList.forEach(tag -> {
                AppPackageTagDO tagDO = AppPackageTagDO.builder()
                        .appId(appId)
                        .appPackageId(appPackageId)
                        .tag(tag)
                        .build();
                appPackageTagService.insert(tagDO);
            });
        }
    }

    /**
     * 创建应用包
     */
    private AppPackageDO createAppPackage(
            AppPackageTaskDO appPackageTaskDO, List<ComponentPackageTaskDO> componentPackageTaskDOList) {
        String appId = appPackageTaskDO.getAppId();
        String packageVersion = appPackageTaskDO.getPackageVersion();

        AppPackageQueryCondition condition = AppPackageQueryCondition.builder()
                .appId(appId)
                .packageVersion(packageVersion)
                .build();
        Pagination<AppPackageDO> appPackages = appPackageService.list(condition);

        // 应用包已存在
        if (!appPackages.isEmpty()) {
            return appPackages.getItems().get(0);
        }

        // 创建过程
        AppPackageDO appPackageDO = AppPackageDO.builder()
                .appId(appId)
                .packageVersion(packageVersion)
                .packageCreator(appPackageTaskDO.getPackageCreator())
                .appSchema(appPackageTaskDO.getPackageOptions())
                .swapp(appPackageTaskDO.getSwapp())
                .componentCount((long) CollectionUtils.size(componentPackageTaskDOList))
                .namespaceId(appPackageTaskDO.getNamespaceId())
                .stageId(appPackageTaskDO.getStageId())
                .build();
        insertAppPackage(appPackageDO, componentPackageTaskDOList);
        return appPackageDO;
    }

    /**
     * 插入指定 appPackage 数据到 DB，并插入相关 rel 引用组件 ID
     *
     * @param appPackageDO               app package 记录
     * @param componentPackageTaskDOList 引用组件
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertAppPackage(AppPackageDO appPackageDO, List<ComponentPackageTaskDO> componentPackageTaskDOList) {
        appPackageRepository.insert(appPackageDO);
        for (ComponentPackageTaskDO componentPackageTaskDO : componentPackageTaskDOList) {
            AppPackageComponentRelDO rel = AppPackageComponentRelDO.builder()
                    .appId(appPackageDO.getAppId())
                    .appPackageId(appPackageDO.getId())
                    .componentPackageId(componentPackageTaskDO.getComponentPackageId())
                    .build();
            appPackageComponentRelRepository.insert(rel);
        }
    }
}
