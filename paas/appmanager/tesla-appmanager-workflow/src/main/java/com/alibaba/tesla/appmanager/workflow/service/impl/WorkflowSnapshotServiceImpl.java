package com.alibaba.tesla.appmanager.workflow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.req.DeleteWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.domain.req.UpdateWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.workflow.repository.WorkflowSnapshotRepository;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowSnapshotQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowSnapshotDO;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowSnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * 对指定的 Workflow 实例 put context 对象，进行 Merge
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param context            Context 对象 (JSONObject)
     * @return 更新后的指定 workflow task 对应的 workflow snapshot 对象
     */
    @Override
    public WorkflowSnapshotDO putContext(Long workflowInstanceId, JSONObject context) {
        if (context == null || context.size() == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "null context to be set");
        }
        List<WorkflowSnapshotDO> result = workflowSnapshotRepository
                .selectByCondition(WorkflowSnapshotQueryCondition.builder()
                        .instanceId(workflowInstanceId)
                        .build());
        if (result.size() == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find workflow snapshot|workflowInstanceId=%d", workflowInstanceId));
        }
        WorkflowSnapshotDO record = result.get(0);
        String dbContextStr = record.getSnapshotContext();
        if (StringUtils.isEmpty(dbContextStr)) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                    String.format("unknown error, the workflow snapshot context is empty|snapshot=%s",
                            JSONObject.toJSONString(record)));
        }
        JSONObject dbContext = JSONObject.parseObject(dbContextStr);
        dbContext.putAll(context);
        record.setSnapshotContext(JSONObject.toJSONString(dbContext));
        int count = workflowSnapshotRepository.updateByCondition(record, WorkflowSnapshotQueryCondition.builder()
                .instanceId(record.getWorkflowInstanceId())
                .taskId(record.getWorkflowTaskId())
                .build());
        if (count != 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot update workflow snapshot when put user custom context|count=%d|record=%s",
                            count, JSONObject.toJSONString(record)));
        }
        return record;
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
            workflowSnapshotRepository.updateByCondition(record, condition);
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
        Pagination<WorkflowSnapshotDO> snapshots = list(WorkflowSnapshotQueryCondition.builder()
                .instanceId(workflowInstanceId)
                .build());
        if (snapshots.getItems().size() == 0) {
            return null;
        }
        String snapshotContextStr = snapshots.getItems().get(0).getSnapshotContext();
        if (StringUtils.isEmpty(snapshotContextStr)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("empty workflow snapshot context|workflowInstanceId=%d", workflowInstanceId));
        }
        try {
            return JSONObject.parseObject(snapshotContextStr);
        } catch (Exception e) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot parse workflow snapshot context to json object|workflowInstanceId=%d|" +
                                    "contextStr=%s|exception=%s", workflowInstanceId, snapshotContextStr,
                            ExceptionUtils.getStackTrace(e)));
        }
    }
}
