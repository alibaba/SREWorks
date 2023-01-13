package com.alibaba.tesla.appmanager.workflow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskStateEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.JsonUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.container.WorkflowGraphNodeId;
import com.alibaba.tesla.appmanager.domain.req.DeleteWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.domain.req.UpdateWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.domain.schema.WorkflowGraph;
import com.alibaba.tesla.appmanager.workflow.repository.WorkflowInstanceRepository;
import com.alibaba.tesla.appmanager.workflow.repository.WorkflowSnapshotRepository;
import com.alibaba.tesla.appmanager.workflow.repository.WorkflowTaskRepository;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowInstanceQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowSnapshotQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowTaskQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowSnapshotDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowSnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Workflow 快照服务实现
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class WorkflowSnapshotServiceImpl implements WorkflowSnapshotService {

    @Autowired
    private WorkflowSnapshotRepository workflowSnapshotRepository;

    @Autowired
    private WorkflowTaskRepository workflowTaskRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    /**
     * 根据 WorkflowInstanceID 获取对应的 WorkflowSnapshot 对象
     *
     * @param workflowSnapshotId Workflow 快照 ID
     * @return WorkflowSnapshot 对象，不存在则返回 null
     */
    @Override
    public WorkflowSnapshotDO get(Long workflowSnapshotId) {
        WorkflowSnapshotQueryCondition condition = WorkflowSnapshotQueryCondition.builder()
                .snapshotId(workflowSnapshotId)
                .build();
        return workflowSnapshotRepository.getByCondition(condition);
    }

    /**
     * 根据条件获取指定的 Workflow Snapshot 记录
     *
     * @param workflowInstanceId Workflow Instance ID
     * @param workflowTaskId     Workflow Task ID
     * @return Workflow 快照对象
     */
    @Override
    public WorkflowSnapshotDO get(Long workflowInstanceId, Long workflowTaskId) {
        WorkflowSnapshotQueryCondition condition = WorkflowSnapshotQueryCondition.builder()
                .instanceId(workflowInstanceId)
                .taskId(workflowTaskId)
                .build();
        List<WorkflowSnapshotDO> snapshots = workflowSnapshotRepository.selectByCondition(condition);
        if (snapshots.size() > 1) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                    String.format("system error, expected 0 or 1 snapshot found|workflowInstanceId=%d|" +
                            "workflowTaskId=%d|size=%d", workflowInstanceId, workflowTaskId, snapshots.size()));
        } else if (snapshots.size() == 1) {
            return snapshots.get(0);
        } else {
            return null;
        }
    }

    /**
     * 根据条件过滤 Workflow 任务列表
     *
     * @param condition 过滤条件
     * @return List of WorkflowSnapshot
     */
    @Override
    public Pagination<WorkflowSnapshotDO> list(WorkflowSnapshotQueryCondition condition) {
        List<WorkflowSnapshotDO> result = workflowSnapshotRepository.selectByCondition(condition);
        return Pagination.valueOf(result, Function.identity());
    }

    /**
     * 对指定的 Workflow 实例 put context 对象，进行 Merge (针对所有当前 PENDING 的任务)
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param context            Context 对象 (JSONObject)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void putContext(Long workflowInstanceId, JSONObject context) {
        if (context == null || context.size() == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "null context to be set");
        }
        List<WorkflowTaskDO> tasks = workflowTaskRepository.selectByCondition(WorkflowTaskQueryCondition.builder()
                .instanceId(workflowInstanceId)
                .taskStatus(WorkflowTaskStateEnum.PENDING.toString())
                .build());
        tasks.forEach(task -> {
            WorkflowSnapshotQueryCondition condition = WorkflowSnapshotQueryCondition.builder()
                    .instanceId(workflowInstanceId)
                    .taskId(task.getId())
                    .build();
            WorkflowSnapshotDO snapshot = workflowSnapshotRepository.getByCondition(condition);
            if (snapshot == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot find workflow snapshot|workflowInstanceId=%d|workflowTaskId=%d",
                                workflowInstanceId, task.getId()));
            }
            String dbContextStr = snapshot.getSnapshotContext();
            if (StringUtils.isEmpty(dbContextStr)) {
                throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                        String.format("unknown error, the workflow snapshot context is empty|snapshot=%s",
                                JSONObject.toJSONString(snapshot)));
            }
            JSONObject dbContext = JSONObject.parseObject(dbContextStr);
            dbContext.putAll(context);
            snapshot.setSnapshotContext(JSONObject.toJSONString(dbContext));
            int count = workflowSnapshotRepository.updateByPrimaryKey(snapshot);
            if (count != 1) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot update workflow snapshot when put user custom context|count=%d|record=%s",
                                count, JSONObject.toJSONString(snapshot)));
            }
        });
    }

    /**
     * 更新一个 Workflow 快照
     *
     * @param request 更新 Workflow 快照请求
     * @return 更新后的 WorkflowSnapshot 对象
     */
    @Override
    public WorkflowSnapshotDO update(UpdateWorkflowSnapshotReq request) {
        WorkflowSnapshotQueryCondition condition = WorkflowSnapshotQueryCondition.builder()
                .instanceId(request.getWorkflowInstanceId())
                .taskId(request.getWorkflowTaskId())
                .build();
        Pagination<WorkflowSnapshotDO> snapshots = list(condition);
        if (snapshots.getItems().size() > 1) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                    String.format("multiple workflow snapshots found|condition=%s",
                            JSONObject.toJSONString(condition)));
        }
        WorkflowSnapshotDO record;
        if (snapshots.isEmpty()) {
            record = WorkflowSnapshotDO.builder()
                    .workflowInstanceId(request.getWorkflowInstanceId())
                    .workflowTaskId(request.getWorkflowTaskId())
                    .snapshotContext(JSONObject.toJSONString(request.getContext()))
                    .snapshotTask(SchemaUtil.toYamlMapStr(request.getConfiguration()))
                    .snapshotWorkflow(null)
                    .build();
            workflowSnapshotRepository.insert(record);
        } else {
            record = snapshots.getItems().get(0);
            record.setSnapshotContext(JSONObject.toJSONString(request.getContext()));
            if (request.getConfiguration() != null) {
                record.setSnapshotTask(SchemaUtil.toYamlMapStr(request.getConfiguration()));
            }
            record.setSnapshotWorkflow(null);
            workflowSnapshotRepository.updateByPrimaryKey(record);
        }
        return get(record.getId());
    }

    /**
     * 根据条件删除 Workflow 快照
     *
     * @param request 删除 Workflow 快照请求
     * @return 删除数量
     */
    @Override
    public int delete(DeleteWorkflowSnapshotReq request) {
        if (request.getWorkflowInstanceId() == null && request.getWorkflowTaskId() == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "empty delete workflow snapshot request");
        }
        WorkflowSnapshotQueryCondition condition = WorkflowSnapshotQueryCondition.builder()
                .instanceId(request.getWorkflowInstanceId())
                .taskId(request.getWorkflowTaskId())
                .build();
        return workflowSnapshotRepository.deleteByCondition(condition);
    }

    /**
     * 获取指定 Workflow Task 对应的 Context JSON Object 对象
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param workflowTaskId     Workflow 任务 ID
     * @return Context JSONObject (不存在时报错)
     */
    @Override
    public JSONObject getContext(Long workflowInstanceId, Long workflowTaskId) {
        Pagination<WorkflowSnapshotDO> snapshots = list(WorkflowSnapshotQueryCondition.builder()
                .instanceId(workflowInstanceId)
                .taskId(workflowTaskId)
                .build());
        if (snapshots.getItems().size() != 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find related workflow snapshot|workflowInstanceId=%d|workflowTaskId=%d|" +
                            "size=%d", workflowInstanceId, workflowTaskId, snapshots.getItems().size()));
        }
        String snapshotContextStr = snapshots.getItems().get(0).getSnapshotContext();
        if (StringUtils.isEmpty(snapshotContextStr)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("empty workflow snapshot context|workflowInstanceId=%d|workflowTaskId=%d",
                            workflowInstanceId, workflowTaskId));
        }
        try {
            return JSONObject.parseObject(snapshotContextStr);
        } catch (Exception e) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot parse workflow snapshot context to json object|workflowInstanceId=%d|" +
                                    "workflowTaskId=%d|contextStr=%s|exception=%s", workflowInstanceId, workflowTaskId,
                            snapshotContextStr, ExceptionUtils.getStackTrace(e)));
        }
    }

    /**
     * 获取指定 Workflow Instance 对应的 Context JSON Object 对象
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @return Context JSONObject (不存在时返回 null)
     */
    @Override
    public JSONObject getContext(Long workflowInstanceId) {
        if (workflowSnapshotRepository.countByCondition(WorkflowSnapshotQueryCondition.builder()
                .instanceId(workflowInstanceId)
                .build()) == 0) {
            return new JSONObject();
        }

        // 如果当前仍然存在 PENDING 的任务，那么说明还没有运行完毕，取任意运行态节点的 Context 作为整体实例 Context
        List<WorkflowTaskDO> tasks = workflowTaskRepository.selectByCondition(WorkflowTaskQueryCondition.builder()
                .instanceId(workflowInstanceId)
                .taskStatusIn(Arrays.asList(
                        WorkflowTaskStateEnum.RUNNING.toString(),
                        WorkflowTaskStateEnum.RUNNING_SUSPEND.toString(),
                        WorkflowTaskStateEnum.WAITING.toString(),
                        WorkflowTaskStateEnum.WAITING_SUSPEND.toString()))
                .build());
        if (tasks.size() > 0) {
            return getContext(workflowInstanceId, tasks.get(0).getId());
        }

        // 如果已经不存在运行态任务，那么根据当前实例的 Graph，获取所有反向入度为 0 的节点，进行 Context 汇聚并返回
        WorkflowInstanceDO instance = workflowInstanceRepository.getByCondition(
                WorkflowInstanceQueryCondition.builder()
                        .instanceId(workflowInstanceId)
                        .withBlobs(true)
                        .build());
        if (instance == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find workflow instance by condition|workflowInstanceId=%d",
                            workflowInstanceId));
        }
        WorkflowGraph graph = JSONObject.parseObject(instance.getWorkflowGraph(), WorkflowGraph.class);
        List<String> reverseNodes = graph.getReverseZeroDegreeNodes();
        if (reverseNodes.size() == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find reverse zero degree nodes in graph|workflowInstanceId=%d",
                            workflowInstanceId));
        }

        // 汇聚 Context (要求至少存在一个 SUCCESS)
        JSONObject context = new JSONObject();
        for (String nodeIdStr : reverseNodes) {
            WorkflowGraphNodeId nodeId = WorkflowGraphNodeId.valueOf(nodeIdStr);
            if (nodeId == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, String.format("invalid nodeId %s", nodeIdStr));
            }
            WorkflowTaskDO task = workflowTaskRepository.getByCondition(WorkflowTaskQueryCondition.builder()
                    .instanceId(workflowInstanceId)
                    .taskType(nodeId.getType())
                    .taskName(nodeId.getName())
                    .build());
            if (task == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot find workflow task by condition|workflowInstanceId=%d|nodeId=%s",
                                workflowInstanceId, nodeIdStr));
            }
            WorkflowSnapshotDO snapshot = get(workflowInstanceId, task.getId());
            if (snapshot == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot find workflow snapshot by condition|workflowInstanceId=%d|" +
                                "workflowTaskId=%d", workflowInstanceId, task.getId()));
            }
            context.putAll(JSONObject.parseObject(snapshot.getSnapshotContext()));
        }
        return context;
    }
}
