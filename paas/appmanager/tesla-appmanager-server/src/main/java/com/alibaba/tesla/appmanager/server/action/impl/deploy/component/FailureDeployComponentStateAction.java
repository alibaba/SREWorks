package com.alibaba.tesla.appmanager.server.action.impl.deploy.component;

import com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant;
import com.alibaba.tesla.appmanager.common.enums.DeployComponentEventEnum;
import com.alibaba.tesla.appmanager.common.enums.DeployComponentStateEnum;
import com.alibaba.tesla.appmanager.common.util.StreamLogHelper;
import com.alibaba.tesla.appmanager.server.action.DeployComponentStateAction;
import com.alibaba.tesla.appmanager.server.event.deploy.DeployComponentEvent;
import com.alibaba.tesla.appmanager.server.event.loader.DeployComponentStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.server.repository.domain.DeployComponentDO;
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

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Component 部署工单 State 处理 Action - FAILURE
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Service("FailureDeployComponentStateAction")
public class FailureDeployComponentStateAction implements DeployComponentStateAction, ApplicationRunner {

    private static final DeployComponentStateEnum STATE = DeployComponentStateEnum.FAILURE;

    private Timer timer;

    private Counter counter;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private StreamLogHelper streamLogHelper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        timer = meterRegistry.timer("deploy.component.status.failure.timer");
        counter = meterRegistry.counter("deploy.component.status.failure.counter");
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
        String cost = subOrder.costTime();
        if (StringUtils.isNumeric(cost)) {
            timer.record(Long.parseLong(cost), TimeUnit.MILLISECONDS);
        }
        counter.increment();
        log.info("deploy component has reached failure state|deployAppId={}|deployComponentId={}|cost={}",
                subOrder.getDeployId(), subOrder.getId(), cost);
        String streamKey = String.format("%s_%s", RedisKeyConstant.DEPLOY_TASK_LOG, subOrder.getId());
        streamLogHelper.clean(streamKey, String.format("deploy component has reached failure state|deployAppId=%s|deployComponentId=%s|cost=%s",
                subOrder.getDeployId(), subOrder.getId(), cost));
        publisher.publishEvent(new DeployComponentEvent(this, DeployComponentEventEnum.TRIGGER_UPDATE, subOrder.getId()));
    }
}
