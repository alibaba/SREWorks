package com.alibaba.tesla.appmanager.server.action.impl.deploy.component;

import com.alibaba.tesla.appmanager.common.enums.DeployComponentAttrTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.DeployComponentEventEnum;
import com.alibaba.tesla.appmanager.common.enums.DeployComponentStateEnum;
import com.alibaba.tesla.appmanager.domain.container.DeployAppRevisionName;
import com.alibaba.tesla.appmanager.server.action.DeployComponentStateAction;
import com.alibaba.tesla.appmanager.server.event.deploy.DeployComponentEvent;
import com.alibaba.tesla.appmanager.server.event.loader.DeployComponentStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.server.repository.condition.RtComponentInstanceQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.DeployComponentDO;
import com.alibaba.tesla.appmanager.server.service.rtcomponentinstance.RtComponentInstanceService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Component 部署工单 State 处理 Action - SUCCESS
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Service("SuccessDeployComponentStateAction")
public class SuccessDeployComponentStateAction implements DeployComponentStateAction, ApplicationRunner {

    private static final DeployComponentStateEnum STATE = DeployComponentStateEnum.SUCCESS;

    private Timer timer;

    private Counter counter;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private RtComponentInstanceService componentInstanceService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        timer = meterRegistry.timer("deploy.component.status.success.timer");
        counter = meterRegistry.counter("deploy.component.status.success.counter");
        publisher.publishEvent(new DeployComponentStateActionLoadedEvent(
                this, STATE.toString(), this.getClass().getSimpleName()));
    }

    /**
     * 自身逻辑处理
     *
     * @param subOrder 部署工单
     * @param attrMap  属性字典
     */
    @Override
    public void run(DeployComponentDO subOrder, Map<String, String> attrMap) {
        // 成功后将 ComponentSchema 数据写入到组件实例表中
        String appId = subOrder.getAppId();
        DeployAppRevisionName revisionName = DeployAppRevisionName.valueOf(subOrder.getIdentifier());
        String componentType = revisionName.getComponentType();
        String componentName = revisionName.getComponentName();
        String clusterId = subOrder.getClusterId();
        String namespaceId = subOrder.getNamespaceId();
        String stageId = subOrder.getStageId();
        String componentSchemaYamlStr = attrMap.get(DeployComponentAttrTypeEnum.COMPONENT_SCHEMA.toString());
        componentInstanceService.reportComponentSchema(RtComponentInstanceQueryCondition.builder()
                .appId(appId)
                .componentType(componentType)
                .componentName(componentName)
                .clusterId(clusterId)
                .namespaceId(namespaceId)
                .stageId(stageId)
                .build(), componentSchemaYamlStr);

        // 计算消耗时间
        String cost = subOrder.costTime();
        if (StringUtils.isNumeric(cost)) {
            timer.record(Long.parseLong(cost), TimeUnit.MILLISECONDS);
        }
        counter.increment();
        log.info("deploy component has reached success state|deployAppId={}|deployComponentId={}|cost={}",
                subOrder.getDeployId(), subOrder.getId(), cost);
        publisher.publishEvent(new DeployComponentEvent(this, DeployComponentEventEnum.TRIGGER_UPDATE, subOrder.getId()));
    }
}
