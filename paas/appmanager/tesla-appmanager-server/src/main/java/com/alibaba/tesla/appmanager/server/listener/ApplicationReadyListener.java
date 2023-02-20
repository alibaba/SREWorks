package com.alibaba.tesla.appmanager.server.listener;

import com.alibaba.tesla.appmanager.server.service.informer.InformerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 服务启动监听
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private InformerManager informerManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            informerManager.init();
            log.info("informer manager has inited");
        } catch (Exception e) {
            log.error("init informer manager failed|exception={}", ExceptionUtils.getStackTrace(e));
        }
    }
}
