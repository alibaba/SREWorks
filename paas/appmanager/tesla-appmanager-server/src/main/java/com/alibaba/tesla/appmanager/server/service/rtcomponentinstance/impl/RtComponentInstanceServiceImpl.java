package com.alibaba.tesla.appmanager.server.service.rtcomponentinstance.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentInstanceStatusEnum;
import com.alibaba.tesla.appmanager.common.enums.DeployComponentAttrTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.DeployComponentStateEnum;
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.InstanceIdUtil;
import com.alibaba.tesla.appmanager.domain.req.componentinstance.ReportRtComponentInstanceStatusReq;
import com.alibaba.tesla.appmanager.dynamicscript.core.GroovyHandlerFactory;
import com.alibaba.tesla.appmanager.server.dynamicscript.handler.ComponentHandler;
import com.alibaba.tesla.appmanager.server.repository.RtComponentInstanceHistoryRepository;
import com.alibaba.tesla.appmanager.server.repository.RtComponentInstanceRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.DeployComponentQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.RtComponentInstanceHistoryQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.RtComponentInstanceQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.RtComponentInstanceDO;
import com.alibaba.tesla.appmanager.server.repository.domain.RtComponentInstanceHistoryDO;
import com.alibaba.tesla.appmanager.server.service.deploy.DeployComponentService;
import com.alibaba.tesla.appmanager.server.service.deploy.business.DeployComponentBO;
import com.alibaba.tesla.appmanager.server.service.rtappinstance.RtAppInstanceService;
import com.alibaba.tesla.appmanager.server.service.rtcomponentinstance.RtComponentInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 组件实例服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j(topic = "status")
public class RtComponentInstanceServiceImpl implements RtComponentInstanceService {

    @Autowired
    private RtComponentInstanceRepository repository;

    @Autowired
    private RtComponentInstanceHistoryRepository historyRepository;

    @Autowired
    private RtAppInstanceService rtAppInstanceService;

    @Autowired
    private GroovyHandlerFactory groovyHandlerFactory;

    @Autowired
    private DeployComponentService deployComponentService;

    /**
     * 刷新指定 records 中的 component_schema 字段
     *
     * @param records 组件实例列表
     */
    @Override
    public List<String> refreshComponentSchema(List<RtComponentInstanceDO> records) {
        List<String> logs = new ArrayList<>();
        for (RtComponentInstanceDO record : records) {
            String appId = record.getAppId();
            String componentType = record.getComponentType();
            String componentName = record.getComponentName();
            String clusterId = record.getClusterId();
            String namespaceId = record.getNamespaceId();
            String stageId = record.getStageId();
            String identifierPrefix = String.format("%s|%s", componentType, componentName);

            String logSuffix = String.format("appId=%s|componentType=%s|componentName=%s|clusterId=%s|namespaceId=%s|" +
                    "stageId=%s", appId, componentType, componentName, clusterId, namespaceId, stageId);
            List<DeployComponentBO> deployComponents = deployComponentService.list(
                    DeployComponentQueryCondition.builder()
                            .appId(appId)
                            .clusterId(clusterId)
                            .namespaceId(namespaceId)
                            .stageId(stageId)
                            .deployStatus(DeployComponentStateEnum.SUCCESS)
                            .identifierStartsWith(identifierPrefix)
                            .pageSize(1)
                            .build(), true);
            if (deployComponents.size() == 0) {
                reportComponentSchema(RtComponentInstanceQueryCondition.builder()
                        .appId(appId)
                        .componentType(componentType)
                        .componentName(componentName)
                        .clusterId(clusterId)
                        .namespaceId(namespaceId)
                        .stageId(stageId)
                        .build(), 0L, 0L, "");
                logs.add(String.format("refresh component schema successfully|%s", logSuffix));
                continue;
            }

            DeployComponentBO deployComponent = deployComponents.get(0);
            Map<String, String> attrMap = deployComponent.getAttrMap();
            String componentSchemaYamlStr = attrMap.get(DeployComponentAttrTypeEnum.COMPONENT_SCHEMA.toString());
            if (StringUtils.isEmpty(componentSchemaYamlStr)) {
                String message = String.format("empty component schema in attrMap in refreshing component" +
                        " schema progress|%s", logSuffix);
                log.warn(message);
                logs.add(message);
                continue;
            }

            // 刷新 component schema yaml
            RtComponentInstanceQueryCondition reportCondition = RtComponentInstanceQueryCondition.builder()
                    .appId(appId)
                    .componentType(componentType)
                    .componentName(componentName)
                    .clusterId(clusterId)
                    .namespaceId(namespaceId)
                    .stageId(stageId)
                    .build();
            reportComponentSchema(reportCondition, deployComponent.getSubOrder().getDeployId(),
                    deployComponent.getSubOrder().getId(), componentSchemaYamlStr);
            logs.add(String.format("refresh component schema successfully|%s", logSuffix));
        }
        return logs;
    }

    /**
     * 上报 ComponentSchema YAML 到组件实例对象中
     *
     * @param condition              组件实例定位条件
     * @param componentSchemaYamlStr 需要上报的 ComponentSchema YAML 内容
     * @param deployAppId            当前对应的应用部署单 ID
     * @param deployComponentId      当前对应的组件部署单 ID
     */
    @Override
    public void reportComponentSchema(
            RtComponentInstanceQueryCondition condition, Long deployAppId,
            Long deployComponentId, String componentSchemaYamlStr) {
        String appId = condition.getAppId();
        String componentType = condition.getComponentType();
        String componentName = condition.getComponentName();
        if (StringUtils.isAnyEmpty(appId, componentType, componentName)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "invalid parameter, appId/componentType/componentName is required");
        }
        String logSuffix = String.format("appId=%s|componentType=%s|componentName=%s|componentSchema=%s",
                appId, componentType, componentName, componentSchemaYamlStr);

        // 确保当前 component schema 一定能够插入到组件实例中
        for (int i = 0; i < DefaultConstant.DB_RETRY_MAX_TIMES; i++) {
            RtComponentInstanceDO record = get(condition);
            if (record == null) {
                log.error("error reporting component schema to realtime component instance, no component instance " +
                        "record|{}", logSuffix);
                return;
            }

            record.setComponentSchema(componentSchemaYamlStr);
            record.setDeployAppId(deployAppId);
            record.setDeployComponentId(deployComponentId);
            int updated = repository.updateByCondition(record, condition);
            if (updated == 0) {
                log.warn("lock failed when reports component schema to realtime component instance, prepare to retry|" +
                        "{}", logSuffix);
                continue;
            }
            log.info("reports component schema to realtime component instance successfully|{}", logSuffix);
            // 触发 app instance 层面的状态更新
            rtAppInstanceService.asyncTriggerStatusUpdate(record.getAppInstanceId());
            break;
        }
    }

    /**
     * 上报原始数据
     *
     * @param record 实时组件实例对象
     */
    @Override
    public void reportRaw(RtComponentInstanceDO record) {
        String componentInstanceId = record.getComponentInstanceId();
        RtComponentInstanceQueryCondition condition = RtComponentInstanceQueryCondition.builder()
                .componentInstanceId(componentInstanceId)
                .build();
        int updated = repository.updateByCondition(record, condition);
        if (updated == 0) {
            log.debug("report request has ignored because of lock version|condition={}",
                    JSONObject.toJSONString(condition));
        } else {
            log.debug("report request has processed|condition={}", JSONObject.toJSONString(condition));
        }

        log.info("action=componentInstanceStatusReportRaw|component instance status has reported|appInstanceId={}|" +
                        "componentInstanceId={}|appId={}|clusterId={}|namespaceId={}|componentName={}|status={}",
                record.getAppInstanceId(), componentInstanceId, record.getAppId(),
                record.getClusterId(), record.getNamespaceId(), record.getComponentName(), record.getStatus());

        // 触发 app instance 层面的状态更新
        rtAppInstanceService.asyncTriggerStatusUpdate(record.getAppInstanceId());
    }

    /**
     * 上报 Component 实例状态
     *
     * @param request     上报数据请求
     * @param ignoreError 是否忽略错误 true or false，错误时抛出 AppException
     */
    @Override
    public void report(ReportRtComponentInstanceStatusReq request, boolean ignoreError) {
        String componentInstanceId = request.getComponentInstanceId();
        RtComponentInstanceQueryCondition condition = RtComponentInstanceQueryCondition.builder()
                .componentInstanceId(componentInstanceId)
                .build();
        String conditionStr = JSONObject.toJSONString(condition);
        List<RtComponentInstanceDO> records = repository.selectByCondition(condition);
        if (records.size() > 1) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                    String.format("multiple realtime component instances found|condition=%s", conditionStr));
        } else if (records.size() == 0) {
            log.error("invalid component instance status report, no related records found in database|request={}",
                    JSONObject.toJSONString(request));
            return;
        }

        // 获取 component type 对象实例
        RtComponentInstanceDO record = records.get(0);
        // 此处不覆盖 appId 内容，避免有自定义 appId 的情况
        record.setComponentType(request.getComponentType());
        record.setComponentName(request.getComponentName());
        record.setVersion(request.getVersion());
        record.setClusterId(request.getClusterId());
        record.setNamespaceId(request.getNamespaceId());
        record.setStageId(request.getStageId());
        record.setVersion(request.getVersion());
        record.setStatus(request.getStatus());
        record.setWatchKind(getWatchKind(request.getComponentType()));
        record.setTimes(record.getTimes());
        record.setConditions(JSONObject.toJSONString(request.getConditions()));
        int updated = repository.updateByCondition(record, condition);
        if (updated == 0) {
            if (ignoreError) {
                log.debug("report request has ignored because of lock version|condition={}", conditionStr);
            } else {
                throw new AppException(AppErrorCode.LOCKER_VERSION_EXPIRED,
                        String.format("report request has ignored because of lock version|condition=%s",
                                conditionStr));
            }
        } else {
            log.debug("report request has processed|condition={}", conditionStr);
        }
        log.info("action=componentInstanceStatusReport|component instance status has reported|appInstanceId={}|" +
                        "componentInstanceId={}|appId={}|clusterId={}|namespaceId={}|componentName={}|status={}",
                record.getAppInstanceId(), componentInstanceId, record.getAppId(),
                request.getClusterId(), request.getNamespaceId(), request.getComponentName(), request.getStatus());

        // 触发 app instance 层面的状态更新
        rtAppInstanceService.asyncTriggerStatusUpdate(record.getAppInstanceId());
    }

    /**
     * 上报 Component 实例状态 (忽略错误)
     *
     * @param request 上报数据请求
     */
    @Override
    public void report(ReportRtComponentInstanceStatusReq request) {
        report(request, true);
    }

    /**
     * 获取实时 component instance 状态列表
     *
     * @param condition 查询条件
     * @return 分页结果
     */
    @Override
    public Pagination<RtComponentInstanceDO> list(RtComponentInstanceQueryCondition condition) {
        List<RtComponentInstanceDO> result = repository.selectByCondition(condition);
        return Pagination.valueOf(result, Function.identity());
    }

    /**
     * 获取实时 component instance 状态历史列表
     *
     * @param condition 查询条件
     * @return 分页结果
     */
    @Override
    public Pagination<RtComponentInstanceHistoryDO> listHistory(RtComponentInstanceHistoryQueryCondition condition) {
        List<RtComponentInstanceHistoryDO> result = historyRepository.selectByCondition(condition);
        return Pagination.valueOf(result, Function.identity());
    }

    /**
     * 查询当前的组件实例，如果存在则返回，否则返回 null
     *
     * @param condition 查询条件 (appId/componentType/componentName/clusterId/namespaceId/stageId 必选,
     *                  clusterId/namespaceId/stage 可为空)
     * @return 实时组件实例 DO 对象 or null
     */
    @Override
    public RtComponentInstanceDO get(RtComponentInstanceQueryCondition condition) {
        return repository.getByCondition(condition);
    }

    /**
     * 查询当前的组件实例，如果存在则返回；否则新增并返回
     *
     * @param condition 查询条件 (appId/componentType/componentName/clusterId/namespaceId/stageId 必选,
     *                  clusterId/namespaceId/stage 可为空)
     * @return 查询或新建后的实时组件实例 DO 对象
     */
    @Override
    public RtComponentInstanceDO getOrCreate(
            RtComponentInstanceQueryCondition condition, String appInstanceId, String version) {
        return getOrCreate(condition, appInstanceId, version, 2);
    }

    /**
     * 查询当前的组件实例，如果存在则返回；否则新增并返回
     *
     * @param condition  查询条件 (appId/componentType/componentName/clusterId/namespaceId/stageId 必选,
     *                   clusterId/namespaceId/stage 可为空)
     * @param retryTimes 重试次数
     * @return 查询或新建后的实时组件实例 DO 对象
     */
    private RtComponentInstanceDO getOrCreate(
            RtComponentInstanceQueryCondition condition, String appInstanceId, String version, int retryTimes) {
        String appId = condition.getAppId();
        String componentType = condition.getComponentType();
        String componentName = condition.getComponentName();
        String clusterId = condition.getClusterId();
        String namespaceId = condition.getNamespaceId();
        String stageId = condition.getStageId();
        if (StringUtils.isAnyEmpty(appId, componentType, componentName, appInstanceId, version)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "invalid parameter, appId/componentType/componentName/appInstanceId/version is required");
        }

        // 查询到直接返回
        RtComponentInstanceDO record = repository.getByCondition(condition);
        if (record != null) {
            return record;
        }

        // 插入新的组件实例记录
        RtComponentInstanceDO instance = RtComponentInstanceDO.builder()
                .appInstanceId(appInstanceId)
                .componentInstanceId(InstanceIdUtil.genComponentInstanceId())
                .appId(appId)
                .componentType(componentType)
                .componentName(componentName)
                .clusterId(clusterId)
                .namespaceId(namespaceId)
                .stageId(stageId)
                .version(version)
                .status(ComponentInstanceStatusEnum.PENDING.toString())
                .watchKind(getWatchKind(componentType))
                .times(0L)
                .conditions("")
                .build();
        try {
            int inserted = repository.insert(instance);
            log.info("action=rtComponentInstance.create|record has inserted|instance={}|inserted={}",
                    JSONObject.toJSONString(instance), inserted);

            // 触发 app instance 层面的状态更新
            rtAppInstanceService.asyncTriggerStatusUpdate(appInstanceId);
        } catch (Exception e) {
            if (retryTimes <= 0) {
                throw e;
            } else {
                return getOrCreate(condition, appInstanceId, version, retryTimes - 1);
            }
        }
        return repository.getByCondition(condition);
    }

    /**
     * 根据组件类型获取 Watch Kind
     *
     * @param componentType 组件类型
     * @return Watch Kind
     */
    private String getWatchKind(String componentType) {
        String watchKind = "";
        ComponentHandler componentHandler = null;
        try {
            componentHandler = groovyHandlerFactory
                    .get(ComponentHandler.class, DynamicScriptKindEnum.COMPONENT.toString(), componentType);
        } catch (Exception e) {
            log.warn("cannot find component type handler by name {}|message={}", componentType, e.getMessage());
        }
        if (componentHandler == null) {
            log.warn("cannot find component type handler by name {}", componentType);
        } else {
            watchKind = componentHandler.watchKind();
        }
        return watchKind;
    }
}
