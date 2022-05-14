package com.alibaba.tesla.appmanager.workflow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskStateEnum;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.req.CreateWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.domain.res.workflow.ExecuteWorkflowHandlerRes;
import com.alibaba.tesla.appmanager.workflow.repository.WorkflowTaskRepository;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowTaskQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowSnapshotDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowSnapshotService;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowTaskService;
import com.alibaba.tesla.appmanager.workflow.service.thread.ExecuteWorkflowTask;
import com.alibaba.tesla.appmanager.workflow.service.thread.ExecuteWorkflowTaskResult;
import com.alibaba.tesla.appmanager.workflow.service.thread.ExecuteWorkflowTaskWaitingObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Workflow Task 服务实现
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

    /**
     * Workflow Task 线程池参数
     */
    private static final int CORE_POOL_SIZE = 20;
    private static final int MAXIMUM_POOL_SIZE = 40;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_SIZE = 1000;

    /**
     * Workflow Task 线程池
     */
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_SIZE),
            r -> new Thread(r, "workflow-task-" + r.hashCode()),
            new ThreadPoolExecutor.AbortPolicy()
    );

    @Autowired
    private WorkflowSnapshotService workflowSnapshotService;

    @Autowired
    private WorkflowTaskRepository workflowTaskRepository;

    /**
     * 根据 WorkflowTaskID 获取对应的 WorkflowTask 对象
     *
     * @param workflowTaskId WorkflowTaskID
     * @param withExt        是否包含扩展信息
     * @return WorkflowTask 对象，不存在则返回 null
     */
    @Override
    public WorkflowTaskDO get(Long workflowTaskId, boolean withExt) {
        WorkflowTaskQueryCondition condition = WorkflowTaskQueryCondition.builder()
                .taskId(workflowTaskId)
                .withBlobs(withExt)
                .build();
        return workflowTaskRepository.getByCondition(condition);
    }

    /**
     * 根据条件过滤 Workflow 任务列表
     *
     * @param condition 过滤条件
     * @return List of WorkflowTask
     */
    @Override
    public Pagination<WorkflowTaskDO> list(WorkflowTaskQueryCondition condition) {
        List<WorkflowTaskDO> result = workflowTaskRepository.selectByCondition(condition);
        return Pagination.valueOf(result, Function.identity());
    }

    /**
     * 更新指定的 Workflow 任务实例
     *
     * @param task Workflow 任务实例
     * @return 更新行数
     */
    @Override
    public int update(WorkflowTaskDO task) {
        log.info("action=updateWorkflowTask|workflowTaskId={}|workflowInstanceId={}|appId={}|status={}",
                task.getId(), task.getWorkflowInstanceId(), task.getAppId(), task.getTaskStatus());
        return workflowTaskRepository.updateByPrimaryKey(task);
    }

    /**
     * 创建一个 Workflow Task 任务 (不触发, 到 PENDING 状态)
     *
     * @param task Workflow 任务实例
     * @return 创建后的 WorkflowTask 对象
     */
    @Override
    public WorkflowTaskDO create(WorkflowTaskDO task) {
        workflowTaskRepository.insert(task);
        return get(task.getId(), true);
    }

    /**
     * 触发执行一个 Workflow Task 任务，并等待其完成 (PENDING -> RUNNING)
     *
     * @param task Workflow 任务实例
     * @return 携带运行信息的 WorkflowTaskDO 实例 (未落库，实例 DO 仅在 events 转换时落库)
     */
    @Override
    public WorkflowTaskDO execute(WorkflowTaskDO task) {
        ExecuteWorkflowTaskWaitingObject waitingObject = ExecuteWorkflowTaskWaitingObject.create(task.getId());
        threadPoolExecutor.submit(new ExecuteWorkflowTask(task));
        ExecuteWorkflowTaskResult result;

        // 等待 Task 任务运行完成
        try {
            result = waitingObject.wait(() -> {
                // 上报心跳
                WorkflowTaskDO current = get(task.getId(), false);
                int count = workflowTaskRepository.updateByPrimaryKey(current);
                if (count == 0) {
                    log.warn("failed to report workflow task heartbeat because of lock version expired|" +
                            "workflowTaskId={}", task.getId());
                } else {
                    log.info("workflow task has been reported heartbeat|workflowTaskId={}", task.getId());
                }
            }, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return markAbnormalWorkflowTask(task.getId(), WorkflowTaskStateEnum.EXCEPTION, e.toString());
        }

        // 如果被终止或未运行成功，保存错误信息到 workflow task 中
        if (result.isTerminated()) {
            return markAbnormalWorkflowTask(task.getId(), WorkflowTaskStateEnum.TERMINATED, result.getExtMessage());
        } else if (result.isPaused()) {
            return markAbnormalWorkflowTask(task.getId(), WorkflowTaskStateEnum.RUNNING_SUSPEND, result.getExtMessage());
        } else if (!result.isSuccess()) {
            return markAbnormalWorkflowTask(task.getId(), WorkflowTaskStateEnum.FAILURE, result.getExtMessage());
        }

        // 创建返回结果
        ExecuteWorkflowHandlerRes output = result.getOutput();
        WorkflowTaskDO returnedTask = get(task.getId(), true);
        returnedTask.setTaskStatus(WorkflowTaskStateEnum.SUCCESS.toString());
        returnedTask.setTaskErrorMessage("");
        if (output.getDeployAppId() != null && output.getDeployAppId() > 0) {
            returnedTask.setDeployAppId(output.getDeployAppId());
        }
        JSONObject context = output.getContext();

        // 保存 Workflow 快照
        WorkflowSnapshotDO snapshot = workflowSnapshotService.create(CreateWorkflowSnapshotReq.builder()
                .workflowTaskId(task.getId())
                .workflowInstanceId(task.getWorkflowInstanceId())
                .context(context)
                .build());
        log.info("workflow snapshot has created|workflowInstanceId={}|workflowTaskId={}|workflowSnapshotId={}|" +
                "context={}", snapshot.getWorkflowInstanceId(), snapshot.getWorkflowTaskId(), snapshot.getId(),
                JSONObject.toJSONString(context));
        return returnedTask;
    }

    /**
     * 终止指定 Workflow 任务 (x -> TERMINATED)
     *
     * @param workflowTaskId WorkflowTaskID
     * @param extMessage     终止时的扩展信息存储字符串
     */
    @Override
    public void terminate(Long workflowTaskId, String extMessage) {
        ExecuteWorkflowTaskWaitingObject.triggerTerminated(workflowTaskId, extMessage);
    }

    /**
     * 暂停指定 Workflow 任务 (RUNNING -> RUNNING_SUSPEND)
     *
     * @param workflowTaskId WorkflowTaskID
     * @param extMessage     暂停时的扩展信息存储字符串
     */
    @Override
    public void suspend(Long workflowTaskId, String extMessage) {
        ExecuteWorkflowTaskWaitingObject.triggerSuspend(workflowTaskId, extMessage);
    }

    /**
     * 标记指定 workflow task 状态 (异常情况，如 FAILURE/EXCEPTION/TERMINATED)
     *
     * @param workflowTaskId Workflow 任务 ID
     * @param status         状态
     * @param errorMessage   错误信息
     */
    private WorkflowTaskDO markAbnormalWorkflowTask(
            Long workflowTaskId, WorkflowTaskStateEnum status, String errorMessage) {
        WorkflowTaskDO task = get(workflowTaskId, true);
        task.setTaskStatus(status.toString());
        task.setTaskErrorMessage(errorMessage);
        return task;
    }
}
