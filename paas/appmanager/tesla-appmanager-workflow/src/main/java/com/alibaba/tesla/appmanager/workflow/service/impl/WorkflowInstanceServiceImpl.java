package com.alibaba.tesla.appmanager.workflow.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.constants.WorkflowConstant;
import com.alibaba.tesla.appmanager.common.enums.WorkflowInstanceEventEnum;
import com.alibaba.tesla.appmanager.common.enums.WorkflowInstanceStateEnum;
import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskEventEnum;
import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskStateEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.container.WorkflowGraphNodeId;
import com.alibaba.tesla.appmanager.domain.option.WorkflowInstanceOption;
import com.alibaba.tesla.appmanager.domain.req.UpdateWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.domain.res.workflow.WorkflowInstanceOperationRes;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.appmanager.domain.schema.WorkflowGraph;
import com.alibaba.tesla.appmanager.workflow.event.WorkflowInstanceEvent;
import com.alibaba.tesla.appmanager.workflow.event.WorkflowTaskEvent;
import com.alibaba.tesla.appmanager.workflow.repository.WorkflowInstanceRepository;
import com.alibaba.tesla.appmanager.workflow.repository.WorkflowTaskRepository;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowInstanceQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowTaskQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowSnapshotDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowInstanceService;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowSnapshotService;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowTaskService;
import com.alibaba.tesla.appmanager.workflow.service.pubsub.WorkflowInstanceMessageConfig;
import com.alibaba.tesla.appmanager.workflow.service.pubsub.WorkflowInstanceOperationCommand;
import com.alibaba.tesla.appmanager.workflow.service.pubsub.WorkflowInstanceOperationCommandWaitingObject;
import com.alibaba.tesla.appmanager.workflow.util.WorkflowNetworkUtil;
import com.google.common.base.Enums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 工作流实例服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private WorkflowTaskRepository workflowTaskRepository;

    @Autowired
    private WorkflowSnapshotService workflowSnapshotService;

    @Autowired
    private WorkflowTaskService workflowTaskService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private WorkflowNetworkUtil workflowNetworkUtil;

    /**
     * 根据 Workflow 实例 ID 获取对应的 Workflow 实例
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param withExt            是否包含扩展信息
     * @return Workflow 实例对象，不存在则返回 null
     */
    @Override
    public WorkflowInstanceDO get(Long workflowInstanceId, boolean withExt) {
        return workflowInstanceRepository.getByCondition(WorkflowInstanceQueryCondition.builder()
                .instanceId(workflowInstanceId)
                .withBlobs(withExt)
                .build());
    }

    /**
     * 根据条件过滤 Workflow 实例列表
     *
     * @param condition 过滤条件
     * @return List
     */
    @Override
    public Pagination<WorkflowInstanceDO> list(WorkflowInstanceQueryCondition condition) {
        List<WorkflowInstanceDO> result = workflowInstanceRepository.selectByCondition(condition);
        return Pagination.valueOf(result, Function.identity());
    }

    /**
     * 覆写 Context 到指定 Workflow 实例
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param context            Context JSONObject
     */
    @Override
    public void putContext(Long workflowInstanceId, JSONObject context) {
        workflowSnapshotService.putContext(workflowInstanceId, context);
    }

    /**
     * 启动一个 Workflow 实例
     *
     * @param appId         应用 ID
     * @param configuration 启动配置
     * @param options       Workflow 配置项
     * @return 启动后的 Workflow 实例
     */
    @Override
    public WorkflowInstanceDO launch(
            String appId, String configuration, WorkflowInstanceOption options) {
        String configurationSha256 = DigestUtils.sha256Hex(configuration);
        DeployAppSchema configurationSchema = SchemaUtil.toSchema(DeployAppSchema.class, configuration);
        enrichConfigurationSchema(configurationSchema);
        WorkflowGraph workflowGraph = WorkflowGraph.valueOf(configurationSchema, options);
        String category = options.getCategory();
        String creator = options.getCreator();
        if (StringUtils.isEmpty(category)) {
            category = DefaultConstant.WORKFLOW_CATEGORY;
        }
        String workflowGraphStr = JSONObject.toJSONString(workflowGraph);
        WorkflowInstanceDO record = WorkflowInstanceDO.builder()
                .appId(appId)
                .category(category)
                .workflowStatus(WorkflowInstanceStateEnum.PENDING.toString())
                .workflowConfiguration(SchemaUtil.toYamlMapStr(configurationSchema))
                .workflowSha256(configurationSha256)
                .workflowOptions(JSONObject.toJSONString(options))
                .workflowCreator(options.getCreator())
                .workflowGraph(workflowGraphStr)
                .gmtStart(DateUtil.now())
                .clientHost(workflowNetworkUtil.getClientHost())
                .build();
        workflowInstanceRepository.insert(record);
        Long workflowInstanceId = record.getId();
        log.info("action=createWorkflowInstance|workflowInstanceId={}|appId={}|creator={}|sha256={}|graph={}|" +
                        "configuration={}", workflowInstanceId, appId, creator, configurationSha256, workflowGraphStr,
                configuration);

        // 事件发送
        WorkflowInstanceEvent event = new WorkflowInstanceEvent(this, WorkflowInstanceEventEnum.START, record);
        eventPublisher.publishEvent(event);
        return record;
    }

    /**
     * 更新一个 Workflow 实例 (by Primary Key)
     *
     * @param workflow Workflow 实例
     */
    @Override
    public void update(WorkflowInstanceDO workflow) {
        log.info("action=updateWorkflowInstance|workflowInstanceId={}|appId={}|category={}|status={}|creator={}",
                workflow.getId(), workflow.getAppId(), workflow.getCategory(), workflow.getWorkflowStatus(),
                workflow.getWorkflowCreator());
        int count = workflowInstanceRepository.updateByPrimaryKey(workflow);
        if (count == 0) {
            throw new AppException(AppErrorCode.LOCKER_VERSION_EXPIRED);
        }
    }

    /**
     * 触发指定 Workflow 实例的后续任务执行
     * <p>
     * 该方法用于 WorkflowInstanceTask 在以 SUCCESS 完成时触发下一个 Task 的执行，并更新 Workflow 实例状态
     *
     * @param instance Workflow 实例
     * @param task     当前执行成功的 Task 实例
     */
    @Override
    public void triggerRestPendingTasks(WorkflowInstanceDO instance, WorkflowTaskDO task) {
        // 当没有新的未完成任务时，说明已经执行完成
        List<WorkflowTaskDO> notFinishedTasks = workflowTaskRepository.notFinishedTasks(instance.getId());
        if (notFinishedTasks.size() == 0) {
            log.info("all workflow instance tasks has finished running, trigger PROCESS_FINISHED event|" +
                    "workflowInstanceId={}", instance.getId());
            publisher.publishEvent(new WorkflowInstanceEvent(this,
                    WorkflowInstanceEventEnum.PROCESS_FINISHED, instance));
            return;
        }

        // 对于当前所有运行完毕且 SUCCESS 的点，在图中进行点的删除，并最后得到入度为 0 的点的列表 (即准备运行的点)
        WorkflowGraph graph = JSONObject.parseObject(instance.getWorkflowGraph(), WorkflowGraph.class);
        List<WorkflowTaskDO> successTasks = workflowTaskRepository.selectByCondition(
                WorkflowTaskQueryCondition.builder()
                        .instanceId(instance.getId())
                        .taskStatus(WorkflowTaskStateEnum.SUCCESS.toString())
                        .build());
        successTasks.stream()
                .map(item -> (new WorkflowGraphNodeId(item.getTaskType(), item.getTaskName()).toString()))
                .forEach(graph::removeNodeInDegree);
        List<String> zeroDegreeNodes = graph.getZeroDegreeNodes();
        log.info("prepare to trigger rest not finished tasks|workflowInstanceId={}|workflowTaskId={}|" +
                "zeroDegreeNodes={}", instance.getId(), task.getId(), JSONArray.toJSONString(zeroDegreeNodes));
        List<WorkflowTaskDO> executeTasks = new ArrayList<>();
        for (String node : zeroDegreeNodes) {
            WorkflowGraphNodeId nodeId = WorkflowGraphNodeId.valueOf(node);
            if (nodeId == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "null zero degree nodes found");
            }
            boolean found = false;
            String foundStatus = "";
            for (WorkflowTaskDO item : notFinishedTasks) {
                if (item.getTaskType().equals(nodeId.getType())
                        && item.getTaskName().equals(nodeId.getName())
                        && item.getTaskStatus().equals(WorkflowTaskStateEnum.PENDING.toString())) {
                    found = true;
                    foundStatus = item.getTaskStatus();
                    executeTasks.add(item);
                }
            }
            if (!found) {
                log.info("the workflow task is not in PENDING status, skip execution|workflowInstanceId={}|" +
                                "workflowTaskId={}|node={}|status={}", instance.getId(), task.getId(),
                        node, foundStatus);
            }
        }
        if (executeTasks.size() == 0) {
            log.info("no more PENDING tasks found, skip execution|workflowInstanceId={}|workflowTaskId={}|" +
                    "zeroDegreeNodes={}", instance.getId(), task.getId(), JSONArray.toJSONString(zeroDegreeNodes));
            return;
        }

        // 对于准备执行的所有 PENDING Task，进行快照汇聚，并触发 START 事件执行
        for (WorkflowTaskDO executeTask : executeTasks) {
            String nodeId = (new WorkflowGraphNodeId(executeTask.getTaskType(), executeTask.getTaskName())).toString();
            List<String> reverseEdges = graph.getReverseEdges().get(nodeId);
            if (reverseEdges == null || reverseEdges.size() == 0) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot find reverse edges by node id|nodeId=%s", nodeId));
            }

            // 获取下一步要运行节点的当前初始化 Context，并合并上游所有 SUCCESS 节点的 Context 到自身
            WorkflowSnapshotDO snapshotDO = workflowSnapshotService.get(instance.getId(), executeTask.getId());
            if (snapshotDO == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot update merged snapshot context because we cannot find the snapshot by " +
                                        "condition|workflowInstanceId=%d|workflowTaskId=%d|executeTaskId=%d",
                                instance.getId(), task.getId(), executeTask.getId()));
            }
            JSONObject snapshotContext = JSONObject.parseObject(snapshotDO.getSnapshotContext());
            for (String parentNodeIdStr : reverseEdges) {
                WorkflowGraphNodeId parentNodeId = WorkflowGraphNodeId.valueOf(parentNodeIdStr);
                if (parentNodeId == null) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS, "null reverse nodes found");
                }
                boolean found = false;
                for (WorkflowTaskDO item : successTasks) {
                    if (item.getTaskType().equals(parentNodeId.getType())
                            && item.getTaskName().equals(parentNodeId.getName())) {
                        found = true;
                        WorkflowSnapshotDO prevSnapshotDO = workflowSnapshotService.get(instance.getId(), item.getId());
                        if (prevSnapshotDO == null || StringUtils.isEmpty(prevSnapshotDO.getSnapshotContext())) {
                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                    String.format("cannot get workflow snapshot by condition|workflowInstanceId=%d|" +
                                            "workflowTaskId=%d", instance.getId(), item.getId()));
                        }
                        JSONObject currentSnapshotContext = JSONObject.parseObject(prevSnapshotDO.getSnapshotContext());
                        snapshotContext.putAll(currentSnapshotContext);
                        log.info("found snapshot context by reverse edges, merged|workflowInstanceId={}|" +
                                "workflowTaskId={}|executeTaskId={}|executeParentTaskId={}|parentSnapshotContext={}",
                                instance.getId(), task.getId(), executeTask.getId(), item.getId(),
                                prevSnapshotDO.getSnapshotContext());
                    }
                }
                if (!found) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("cannot find parent node in workflow task db|workflowInstanceId=%d|" +
                                    "workflowTaskId=%d|executeTaskId=%d|parentNodeId=%s", instance.getId(),
                                    task.getId(), executeTask.getId(), parentNodeIdStr));
                }
            }
            log.info("snapshot context has merged|workflowInstanceId={}|workflowTaskId={}|executeTaskId={}|" +
                            "context={}", instance.getId(), task.getId(), executeTask.getId(),
                    JSONObject.toJSONString(snapshotContext));

            // 更新 snapshot 到准备执行的任务 task 中
            workflowSnapshotService.update(UpdateWorkflowSnapshotReq.builder()
                .workflowInstanceId(instance.getId())
                .workflowTaskId(executeTask.getId())
                .context(snapshotContext)
                .build());
            log.info("found the next workflow task to run, trigger START|workflowInstanceId={}|" +
                            "currentTaskId={}|nextTaskId={}|appId={}|taskType={}|taskName={}|taskStage={}|context={}",
                    instance.getId(), task.getId(), executeTask.getId(), executeTask.getAppId(),
                    executeTask.getTaskType(), executeTask.getTaskName(), executeTask.getTaskStage(),
                    JSONObject.toJSONString(snapshotContext));
            publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.START, executeTask));
        }
    }

    /**
     * 对指定 workflow instance 触发失败事件
     *
     * @param instance     WorkflowInstance
     * @param errorMessage 错误信息
     */
    @Override
    public void triggerProcessFailed(WorkflowInstanceDO instance, String errorMessage) {
        instance.setWorkflowErrorMessage(errorMessage);
        log.info("the current workflow instance is triggered by a failure event|workflowInstanceId={}|appId={}|" +
                        "category={}|errorMessage={}", instance.getId(), instance.getAppId(), instance.getCategory(),
                errorMessage);
        publisher.publishEvent(new WorkflowInstanceEvent(this, WorkflowInstanceEventEnum.PROCESS_FAILED, instance));
    }

    /**
     * 对指定 workflow instance 触发异常事件
     *
     * @param instance     WorkflowInstance
     * @param errorMessage 错误信息
     */
    @Override
    public void triggerProcessUnknownError(WorkflowInstanceDO instance, String errorMessage) {
        instance.setWorkflowErrorMessage(errorMessage);
        log.warn("the current workflow instance is triggered by an exception event|workflowInstanceId={}|appId={}|" +
                        "category={}|errorMessage={}", instance.getId(), instance.getAppId(), instance.getCategory(),
                errorMessage);
        publisher.publishEvent(new WorkflowInstanceEvent(this,
                WorkflowInstanceEventEnum.UNKNOWN_ERROR, instance));
    }

    /**
     * 触发指定 Workflow 实例的状态更新，根据当前所有子 Task 的状态进行汇聚更新
     * <p>
     * 该方法用于 WorkflowInstanceTask 在以非 SUCCESS 完成时触发 Workflow 实例状态更新
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @return 更新状态后的 Workflow 实例
     */
    @Override
    public WorkflowInstanceDO triggerUpdate(Long workflowInstanceId) {
        return null;
    }

    /**
     * 对指定 workflow instance 触发终止事件
     *
     * @param instance     WorkflowInstance
     * @param errorMessage 错误信息
     */
    @Override
    public void triggerOpTerminate(WorkflowInstanceDO instance, String errorMessage) {
        instance.setWorkflowErrorMessage(errorMessage);
        log.info("the current workflow instance is triggered by OP_TERMINATE|workflowInstanceId={}|appId={}|" +
                        "category={}|errorMessage={}", instance.getId(), instance.getAppId(), instance.getCategory(),
                errorMessage);
        publisher.publishEvent(new WorkflowInstanceEvent(this, WorkflowInstanceEventEnum.OP_TERMINATE, instance));
    }

    /**
     * 对指定 workflow instance 触发暂停事件
     *
     * @param instance WorkflowInstance
     */
    @Override
    public void triggerPause(WorkflowInstanceDO instance) {
        log.info("the current workflow instance is triggered by PAUSE|workflowInstanceId={}|appId={}|category={}",
                instance.getId(), instance.getAppId(), instance.getCategory());
        publisher.publishEvent(new WorkflowInstanceEvent(this, WorkflowInstanceEventEnum.PAUSE, instance));
    }

    /**
     * 广播操作: 发送给当前 Workflow 实例命令
     * <p>
     * 等待结果最长 5s。如果没有响应，那么尝试将当前 workflow instance Owner 更新为自身
     * <p>
     * 如果更新 Owner 成功，开始本地命令执行；更新 Owner 失败则报错 (可能网络分区)
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param command            命令
     * @return 操作执行结果
     */
    @Override
    public WorkflowInstanceOperationRes broadcastCommand(Long workflowInstanceId, String command) {
        WorkflowInstanceDO workflowInstance = get(workflowInstanceId, true);
        if (workflowInstance == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find workflow instance by id %d", workflowInstanceId));
        }

        // 发送广播信号并等待某节点回应
        log.info("prepare to {} workflow instance and send the broadcast message to all nodes|" +
                "workflowInstanceId={}", command, workflowInstanceId);
        WorkflowInstanceOperationCommandWaitingObject waitingObject = WorkflowInstanceOperationCommandWaitingObject
                .create(workflowInstanceId, command);
        broadcastCommandMessage(command, workflowInstanceId);
        log.info("broadcast message of {} workflow instance has sent to redis channel|workflowInstanceId={}",
                command, workflowInstanceId);
        WorkflowInstanceOperationRes executeResult;
        try {
            executeResult = waitingObject.wait(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new AppException(AppErrorCode.COMMAND_ERROR,
                    String.format("%s failed because of InterruptedException", command));
        }

        // 如果等到回来的广播消息，那么直接返回
        if (executeResult != null) {
            log.info("waiting for the execution of the {} command to complete, and the execution result " +
                            "message has returned|workflowInstanceId={}|result={}", command, workflowInstanceId,
                    JSONObject.toJSONString(executeResult));
            return executeResult;
        }

        // 没有等到返回，说明可能该任务可能因为意外已经失去托管，那么尝试更新 workflow instance Owner 到自身
        log.info("waiting for the execution of the {} command to complete failed, prepare to take control " +
                "of the workflow instance|workflowInstanceId={}", command, workflowInstanceId);
        String clientHost = workflowNetworkUtil.getClientHost();
        workflowInstance.setClientHost(clientHost);
        try {
            update(workflowInstance);
        } catch (AppException e) {
            if (e.getErrorCode().equals(AppErrorCode.LOCKER_VERSION_EXPIRED)) {
                throw new AppException(AppErrorCode.COMMAND_ERROR,
                        "waiting broadcast message timeout and fight for owner failed");
            } else {
                throw e;
            }
        }

        // 更新 owner 成功，则直接执行本地命令 (true 为响应信号; false 为其他可静默情况, 如多次并发执行)
        log.info("workflow instance control has been acquired, prepare to {} it in local node|" +
                "workflowInstanceId={}", command, workflowInstanceId);
        boolean success;
        if (WorkflowInstanceOperationCommand.COMMAND_TERMINATE.equals(command)) {
            success = localTerminate(workflowInstance);
        } else if (WorkflowInstanceOperationCommand.COMMAND_RESUME.equals(command)) {
            success = localResume(workflowInstance);
        } else if (WorkflowInstanceOperationCommand.COMMAND_RETRY.equals(command)) {
            success = localRetry(workflowInstance);
        } else {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "invalid command " + command);
        }
        if (success) {
            return WorkflowInstanceOperationRes.builder()
                    .command(command)
                    .workflowInstanceId(workflowInstanceId)
                    .clientHost(clientHost)
                    .success(true)
                    .message("")
                    .build();
        } else {
            return WorkflowInstanceOperationRes.builder()
                    .command(command)
                    .workflowInstanceId(workflowInstanceId)
                    .clientHost(clientHost)
                    .success(false)
                    .message("")
                    .build();
        }
    }

    /**
     * 当前节点操作: 终止当前 Workflow 实例
     *
     * @param instance Workflow 实例
     * @return 是否成功 (本地存在任务且响应了终止信号返回 true，其他可静默情况返回 false，非法情况抛异常)
     */
    @Override
    public boolean localTerminate(WorkflowInstanceDO instance) {
        // 如果当前 workflow instance 实例的 client_host 不属于自己，那么直接返回 false 表终止失败
        if (!workflowNetworkUtil.getClientHost().equals(instance.getClientHost())) {
            return false;
        }

        // 明确了所有权之后开始扫描并执行操作，幂等，状态不管怎么变，都返回 true（出现未知异常除外）
        Long workflowInstanceId = instance.getId();
        WorkflowInstanceStateEnum instanceStatus = Enums
                .getIfPresent(WorkflowInstanceStateEnum.class, instance.getWorkflowStatus()).orNull();
        if (instanceStatus == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("[local] invalid workflow instance status|workflowInstanceId=%d|instanceStatus=%s",
                            instance.getId(), instance.getWorkflowStatus()));
        }
        if (instanceStatus.isFinalState()) {
            log.info("[local] the workflow instance is already in final state and cannot be terminated again|" +
                    "workflowInstanceId={}", workflowInstanceId);
            return true;
        }

        // 对当前所有该 workflow instance 下面的 tasks 中处于 RUNNING 的发送终止信号
        Pagination<WorkflowTaskDO> tasks = workflowTaskService.list(WorkflowTaskQueryCondition.builder()
                .instanceId(instance.getId())
                .withBlobs(false)
                .build());
        boolean found = false;
        for (WorkflowTaskDO task : tasks.getItems()) {
            WorkflowTaskStateEnum taskStatus = Enums
                    .getIfPresent(WorkflowTaskStateEnum.class, task.getTaskStatus()).orNull();
            if (taskStatus == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("[local] invalid workflow task status|workflowInstanceId=%d|workflowTaskId=%d|" +
                                "taskStatus=%s", instance.getId(), task.getId(), task.getTaskStatus()));
            }

            if (workflowTaskService.terminate(task.getId(), "terminated")) {
                log.info("[local] interrupt has sent to running workflow task|workflowInstanceId={}|workflowTaskId={}",
                        workflowInstanceId, task.getId());
                found = true;
            }
        }

        // 如果发现存在 tasks 响应了终止信号，说明存在流程会自动触发后续的 workflow instance 的状态更新
        if (found) {
            log.info("[local] terminate success, return immediately|workflowInstanceId={}", workflowInstanceId);
            return true;
        }

        // 如果没有发现任何 tasks 可发送终止信号，那么尝试直接发送 OP_TERMINATE 到每个非终态的 tasks
        log.info("[local] cannot find any subtasks to send interrupt signal, prepare to send OP_TERMINATE to all " +
                "subtasks|workflowInstanceId={}", instance.getId());
        for (WorkflowTaskDO task : tasks.getItems()) {
            WorkflowTaskStateEnum taskStatus = Enums
                    .getIfPresent(WorkflowTaskStateEnum.class, task.getTaskStatus()).orNull();
            if (taskStatus != null && !taskStatus.isFinalState() && !taskStatus.equals(WorkflowTaskStateEnum.PENDING)) {
                log.info("[local] prepare to send OP_TERMINATE event to workflow task|workflowInstanceId={}|" +
                        "workflowTaskId={}", workflowInstanceId, task.getId());
                publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.OP_TERMINATE, task));
            }
        }
        return true;
    }

    /**
     * 恢复处于 SUSPEND 状态的 Workflow 实例
     *
     * @param instance Workflow 实例
     * @return 是否成功 (本地存在任务且响应了终止信号返回 true，其他可静默情况返回 false，非法情况抛异常)
     */
    @Override
    public boolean localResume(WorkflowInstanceDO instance) {
        // 如果当前 workflow instance 实例的 client_host 不属于自己，那么直接返回 false 表恢复失败
        if (!workflowNetworkUtil.getClientHost().equals(instance.getClientHost())) {
            return false;
        }

        Long workflowInstanceId = instance.getId();
        String appId = instance.getAppId();
        String category = instance.getCategory();
        log.info("prepare to resume the workflow instance|workflowInstanceId={}|appId={}|category={}",
                workflowInstanceId, appId, category);

        // 发送 RESUME 事件到 workflow instance
        publisher.publishEvent(new WorkflowInstanceEvent(this, WorkflowInstanceEventEnum.RESUME, instance));
        log.info("the RESUME event has sent to workflow instance|workflowInstanceId={}|appId={}|previousStatus={}",
                workflowInstanceId, appId, instance.getWorkflowStatus());

        // 遍历所有子任务，对于当前仍然处于 RUNNING_SUSPEND 或 WAITING_SUSPEND 状态的，进行 RESUME 事件发送
        Pagination<WorkflowTaskDO> tasks = workflowTaskService.list(WorkflowTaskQueryCondition.builder()
                .instanceId(instance.getId())
                .withBlobs(false)
                .build());
        for (WorkflowTaskDO task : tasks.getItems()) {
            WorkflowTaskStateEnum status = Enums
                    .getIfPresent(WorkflowTaskStateEnum.class, task.getTaskStatus()).orNull();
            if (status == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("invalid workflow task status|workflowInstanceId=%d|workflowTaskId=%d|" +
                                "taskStatus=%s", instance.getId(), task.getId(), task.getTaskStatus()));
            }
            if (WorkflowTaskStateEnum.RUNNING_SUSPEND.equals(status)
                    || WorkflowTaskStateEnum.WAITING_SUSPEND.equals(status)) {
                publisher.publishEvent(new WorkflowTaskEvent(this, WorkflowTaskEventEnum.RESUME, task));
                log.info("the RESUME event has sent to workflow task|workflowInstanceId={}|workflowTaskId={}|" +
                                "taskType={}|taskName={}|appId={}|previousStatus={}", workflowInstanceId,
                        task.getId(), task.getTaskType(), task.getTaskName(), appId, status);
            }
        }
        return true;
    }

    /**
     * 重试当前已经到达终态的 Workflow 实例 (SUCCESS/FAILURE/EXCEPTION/TERMINATED)
     * <p>
     * 注意该方法将会从第一个节点开始，使用原始参数重新运行一遍当前 Workflow 实例
     *
     * @param instance Workflow 实例
     * @return 是否成功 (本地存在任务且响应了终止信号返回 true，其他可静默情况返回 false，非法情况抛异常)
     */
    @Override
    public boolean localRetry(WorkflowInstanceDO instance) {
        // 如果当前 workflow instance 实例的 client_host 不属于自己，那么直接返回 false 表恢复失败
        if (!workflowNetworkUtil.getClientHost().equals(instance.getClientHost())) {
            return false;
        }

        // TODO

        return true;
    }

    /**
     * 重试当前已经到达终态的 Workflow 实例 (SUCCESS/FAILURE/EXCEPTION/TERMINATED)
     * <p>
     * 注意该方法从指定 taskId 开始进行重试，即重新运行 taskId 及之后的所有 WorkflowInstance 任务
     * <p>
     * 该方法会获取 taskId 对应的快照内容，以此为输入进行重试
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param workflowTaskId     Workflow Task ID
     * @return 更新状态后的 Workflow 实例
     */
    @Override
    public WorkflowInstanceDO localRetryFromTask(Long workflowInstanceId, Long workflowTaskId) {
        return null;
    }

    /**
     * 获取指定应用指定 category 的最后一个 SUCCESS 状态的 Workflow 实例
     *
     * @param appId    应用 ID
     * @param category 分类
     * @return WorkflowInstanceDO
     */
    @Override
    public WorkflowInstanceDO getLastSuccessInstance(String appId, String category) {
        WorkflowInstanceQueryCondition condition = WorkflowInstanceQueryCondition.builder()
                .appId(appId)
                .category(category)
                .workflowStatus(WorkflowInstanceStateEnum.SUCCESS.toString())
                .pageSize(1)
                .withBlobs(true)
                .build();
        List<WorkflowInstanceDO> results = workflowInstanceRepository.selectByCondition(condition);
        if (results.size() == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find last succeed workflow instance|appId=%s|category=%s", appId, category));
        }
        return results.get(0);
    }

    /**
     * 丰富 configuration schema 内容，如果默认不存在任何 workflow task，那么默认填充
     *
     * @param configurationSchema Workflow Configuration Schema
     */
    private void enrichConfigurationSchema(DeployAppSchema configurationSchema) {
        DeployAppSchema.Workflow workflow = configurationSchema.getSpec().getWorkflow();
        if (workflow == null || workflow.getSteps() == null || workflow.getSteps().size() == 0) {
            DeployAppSchema.Workflow defaultWorkflow = DeployAppSchema.Workflow.builder()
                    .steps(List.of(DeployAppSchema.WorkflowStep.builder()
                            .type(WorkflowConstant.DEFAULT_WORKFLOW_TYPE)
                            .name(WorkflowConstant.DEFAULT_WORKFLOW_TYPE)
                            .stage(WorkflowConstant.DEFAULT_WORKFLOW_STAGE.toString())
                            .inputs(new ArrayList<>())
                            .outputs(new ArrayList<>())
                            .properties(new JSONObject())
                            .build()))
                    .build();
            configurationSchema.getSpec().setWorkflow(defaultWorkflow);
        }

        // 如果 taskName 不存在的话，补充 taskName 到每个 workflow steps 中
        for (int i = 0; i < workflow.getSteps().size(); i++) {
            DeployAppSchema.WorkflowStep step = workflow.getSteps().get(i);
            String type = step.getType();
            if (StringUtils.isEmpty(type)) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "empty type in workflow steps");
            }
            String name = step.getName();
            if (StringUtils.isEmpty(name)) {
                step.setName(String.format("%s-%d", type, i));
            }
        }
    }

    /**
     * 发送广播命令消息
     *
     * @param command            命令
     * @param workflowInstanceId Workflow Instance ID
     */
    private void broadcastCommandMessage(String command, Long workflowInstanceId) {
        redisTemplate.convertAndSend(WorkflowInstanceMessageConfig.TOPIC_WORKFLOW_INSTANCE_OPERATION_COMMAND,
                JSONObject.toJSONString(WorkflowInstanceOperationCommand.builder()
                        .command(command)
                        .workflowInstanceId(workflowInstanceId)
                        .build()));
    }
}
