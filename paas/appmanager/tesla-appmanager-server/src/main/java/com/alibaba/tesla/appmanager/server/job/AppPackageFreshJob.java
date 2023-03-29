package com.alibaba.tesla.appmanager.server.job;

import com.alibaba.tesla.appmanager.autoconfig.SystemProperties;
import com.alibaba.tesla.appmanager.common.enums.AppPackageTaskStatusEnum;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.server.repository.condition.AppPackageTaskQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppPackageTaskDO;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageTaskService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author qianmo.zm@alibaba-inc.com
 * @date 2020/07/14.
 */
@Slf4j(topic = "job")
@Component
public class AppPackageFreshJob {

    @Autowired
    private AppPackageTaskService appPackageTaskService;

    @Autowired
    private SystemProperties systemProperties;

    @Scheduled(cron = "${appmanager.cron-job.app-refresh}")
    @SchedulerLock(name = "appPackageFreshJob", lockAtLeastFor = "4s")
    public void scheduledTask() {
        AppPackageTaskQueryCondition condition = AppPackageTaskQueryCondition.builder()
                .taskStatusList(Arrays.asList(
                        AppPackageTaskStatusEnum.CREATED,
                        AppPackageTaskStatusEnum.COM_PACK_RUN,
                        AppPackageTaskStatusEnum.APP_PACK_RUN))
                .envId(systemProperties.getEnvId())
                .withBlobs(true)
                .build();
        Pagination<AppPackageTaskDO> tasks = appPackageTaskService.list(condition);
        if (tasks.getTotal() > 0) {
            log.info("action=appPackageFreshJob|freshAppPackageTask|taskCount={}", tasks.getTotal());
        } else {
            log.debug("action=appPackageFreshJob|freshAppPackageTask|taskCount={}", tasks.getTotal());
        }
        if (!tasks.isEmpty()) {
            tasks.getItems().forEach(item -> {
                try {
                    appPackageTaskService.freshAppPackageTask(item);
                } catch (Exception e) {
                    log.error("action=appPackageFreshJob|freshAppPackageTask|e={}", e.getMessage(), e);
                }
            });
        }
    }
}
