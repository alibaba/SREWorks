package com.alibaba.tesla.appmanager.server.action.impl.componentpackage;

import com.alibaba.tesla.appmanager.common.enums.ComponentPackageTaskStateEnum;
import com.alibaba.tesla.appmanager.server.action.ComponentPackageTaskStateAction;
import com.alibaba.tesla.appmanager.server.event.loader.ComponentPackageTaskStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.server.repository.domain.ComponentPackageTaskDO;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Component Package State 处理 Action - SUCCESS
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Service("SuccessComponentPackageTaskStateAction")
public class SuccessComponentPackageTaskStateAction implements ComponentPackageTaskStateAction, ApplicationRunner {

    private static final String LOG_PRE = String.format("action=action.%s|message=",
            SuccessComponentPackageTaskStateAction.class.getSimpleName());

    private static final ComponentPackageTaskStateEnum STATE = ComponentPackageTaskStateEnum.SUCCESS;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private AppPackageTaskService appPackageTaskService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        publisher.publishEvent(new ComponentPackageTaskStateActionLoadedEvent(
                this, STATE.toString(), this.getClass().getSimpleName()));
    }

    /**
     * 自身逻辑处理
     *
     * @param task 部署工单
     */
    @Override
    public void run(ComponentPackageTaskDO task) {
        appPackageTaskService.updateComponentTaskStatus(task, STATE);
        log.info("reached success state|componentPackageTaskId={}", task.getId());
    }
}
