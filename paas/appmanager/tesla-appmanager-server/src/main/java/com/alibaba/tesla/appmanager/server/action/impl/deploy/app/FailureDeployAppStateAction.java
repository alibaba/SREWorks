package com.alibaba.tesla.appmanager.server.action.impl.deploy.app;

import com.alibaba.tesla.appmanager.common.enums.DeployAppStateEnum;
import com.alibaba.tesla.appmanager.server.action.DeployAppStateAction;
import com.alibaba.tesla.appmanager.server.event.loader.DeployAppStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.server.repository.domain.DeployAppDO;
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
 * App 部署工单 State 处理 Action - FAILURE
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Service("FailureDeployAppStateAction")
public class FailureDeployAppStateAction implements DeployAppStateAction, ApplicationRunner {

    private static final DeployAppStateEnum STATE = DeployAppStateEnum.FAILURE;

    private Timer timer;

    private Counter counter;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        timer = meterRegistry.timer("deploy.app.status.failure.timer");
        counter = meterRegistry.counter("deploy.app.status.failure.counter");
        publisher.publishEvent(new DeployAppStateActionLoadedEvent(
                this, STATE.toString(), this.getClass().getSimpleName()));
    }

    /**
     * 自身逻辑处理
     *
     * @param order   部署工单
     * @param attrMap 部署属性字典
     */
    @Override
    public void run(DeployAppDO order, Map<String, String> attrMap) {
        String cost = order.costTime();
        if (StringUtils.isNumeric(cost)) {
            timer.record(Long.parseLong(cost), TimeUnit.MILLISECONDS);
        }
        counter.increment();
        log.info("deploy app has reached failure state|deployAppId={}|appPackageId={}|cost={}",
                order.getId(), order.getAppPackageId(), cost);
    }
}
