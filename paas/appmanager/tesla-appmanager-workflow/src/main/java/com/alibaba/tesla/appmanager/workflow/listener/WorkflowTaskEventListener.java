package com.alibaba.tesla.appmanager.workflow.listener;

import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskEventEnum;
import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskStateEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.workflow.action.WorkflowTaskStateAction;
import com.alibaba.tesla.appmanager.workflow.action.WorkflowTaskStateActionManager;
import com.alibaba.tesla.appmanager.workflow.event.WorkflowTaskEvent;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowInstanceService;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowTaskService;
import com.google.common.base.Enums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Workflow Task 事件监听器
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Component
public class WorkflowTaskEventListener implements ApplicationListener<WorkflowTaskEvent> {

    @Autowired
    private WorkflowTaskStateActionManager workflowTaskStateActionManager;

    @Autowired
    private WorkflowTaskService workflowTaskService;

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * 处理 App 部署单事件
     *
     * @param event 事件
     */
    @Async
    @Override
    public void onApplicationEvent(WorkflowTaskEvent event) {
        Long workflowTaskId = event.getTask().getId();
        WorkflowTaskEventEnum currentEvent = event.getEvent();
        String logPre = String.format("action=event.app.%s|message=", currentEvent.toString());
        WorkflowTaskDO task = workflowTaskService.get(workflowTaskId, false);

        // 进行状态检测
        WorkflowTaskStateEnum status = Enums.getIfPresent(WorkflowTaskStateEnum.class,
                task.getTaskStatus()).orNull();
        if (status == null) {
            log.error(logPre + "invalid event, cannot identify current status|workflowTaskId={}|" +
                    "status={}", workflowTaskId, task.getTaskStatus());
            return;
        }
        WorkflowTaskStateEnum nextStatus = status.next(currentEvent);
        if (nextStatus == null) {
            log.warn(logPre + "invalid event, cannot transform to next status|workflowTaskId={}|" +
                    "status={}", workflowTaskId, task.getTaskStatus());
            return;
        }

        // 状态转移
        String logSuffix = String.format("|workflowTaskId=%d|fromStatus=%s|toStatus=%s|errorMessage=%s",
                workflowTaskId, status, nextStatus, event.getTask().getTaskErrorMessage());
        task.setTaskStatus(nextStatus.toString());
        if (task.getGmtStart() == null || task.getGmtStart().getTime() == 0L) {
            task.setGmtStart(DateUtil.now());
        }
        task.setGmtEnd(DateUtil.now());
        // maybe "", it's ok
        if (event.getTask().getTaskErrorMessage() != null) {
            task.setTaskErrorMessage(event.getTask().getTaskErrorMessage());
        }
        if (event.getTask().getDeployAppId() != null && event.getTask().getDeployAppId() > 0) {
            task.setDeployAppId(event.getTask().getDeployAppId());
            task.setDeployAppUnitId(event.getTask().getDeployAppUnitId());
            task.setDeployAppNamespaceId(event.getTask().getDeployAppNamespaceId());
            task.setDeployAppStageId(event.getTask().getDeployAppStageId());
            logSuffix += String.format("|deployAppId=%d|deployAppUnitId=%s|deployAppNamespaceId=%s|deployAppStageId=%s",
                    event.getTask().getDeployAppId(), event.getTask().getDeployAppUnitId(),
                    event.getTask().getDeployAppNamespaceId(),
                    event.getTask().getDeployAppStageId());
        }
        if (event.getTask().getDeployWorkflowInstanceId() != null && event.getTask().getDeployWorkflowInstanceId() > 0) {
            task.setDeployWorkflowInstanceId(event.getTask().getDeployWorkflowInstanceId());
            logSuffix += String.format("|deployWorkflowInstanceId=%d", event.getTask().getDeployWorkflowInstanceId());
        }
        try {
            workflowTaskService.update(task);
        } catch (AppException e) {
            if (AppErrorCode.LOCKER_VERSION_EXPIRED.equals(e.getErrorCode())) {
                log.info(logPre + "locker version expired, skip" + logSuffix);
                return;
            }
        }
        if (!status.toString().equals(nextStatus.toString())) {
            log.info(logPre + "status has changed" + logSuffix);
        }

        // 运行目标 State 的动作
        WorkflowTaskStateAction instance = workflowTaskStateActionManager.getInstance(nextStatus.toString());
        task = workflowTaskService.get(workflowTaskId, true);
        try {
            instance.run(task);
        } catch (AppException e) {
            if (AppErrorCode.LOCKER_VERSION_EXPIRED.equals(e.getErrorCode())) {
                log.info(logPre + "locker version expired, skip" + logSuffix);
                return;
            }
            triggerException(task, e.getErrorMessage());
        } catch (Exception e) {
            triggerException(task, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 触发 Exception 异常，并写入异常信息
     *
     * @param task         任务对象
     * @param errorMessage 错误信息
     */
    private void triggerException(WorkflowTaskDO task, String errorMessage) {
        task.setTaskStatus(WorkflowTaskStateEnum.EXCEPTION.toString());
        task.setTaskErrorMessage(errorMessage);
        publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.UNKNOWN_ERROR, task));
    }
}
