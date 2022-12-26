package com.alibaba.tesla.appmanager.workflow.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.req.UpdateWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.domain.req.DeleteWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowSnapshotQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowSnapshotDO;

/**
 * 工作流快照服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface WorkflowSnapshotService {

    /**
     * 根据 WorkflowInstanceID 获取对应的 WorkflowSnapshot 对象
     *
     * @param workflowSnapshotId Workflow 快照 ID
     * @return WorkflowSnapshot 对象，不存在则返回 null
     */
    WorkflowSnapshotDO get(Long workflowSnapshotId);

    /**
     * 根据条件获取指定的 Workflow Snapshot 记录
     *
     * @param workflowInstanceId Workflow Instance ID
     * @param workflowTaskId     Workflow Task ID
     * @return Workflow 快照对象
     */
    WorkflowSnapshotDO get(Long workflowInstanceId, Long workflowTaskId);

    /**
     * 根据条件过滤 Workflow 任务列表
     *
     * @param condition 过滤条件
     * @return List of WorkflowSnapshot
     */
    Pagination<WorkflowSnapshotDO> list(WorkflowSnapshotQueryCondition condition);

    /**
     * 对指定的 Workflow 实例 put context 对象，进行 Merge
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param context            Context 对象 (JSONObject)
     */
    void putContext(Long workflowInstanceId, JSONObject context);

    /**
     * 更新一个 Workflow 快照
     *
     * @param request 更新 Workflow 快照请求
     * @return 更新后的 WorkflowSnapshot 对象
     */
    WorkflowSnapshotDO update(UpdateWorkflowSnapshotReq request);

    /**
     * 根据条件删除 Workflow 快照
     *
     * @param request 删除 Workflow 快照请求
     * @return 删除数量
     */
    int delete(DeleteWorkflowSnapshotReq request);

    /**
     * 获取指定 Workflow Task 对应的 Context JSON Object 对象
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param workflowTaskId     Workflow 任务 ID
     * @return Context JSONObject
     */
    JSONObject getContext(Long workflowInstanceId, Long workflowTaskId);

    /**
     * 获取指定 Workflow Instance 对应的 Context JSON Object 对象
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @return Context JSONObject
     */
    JSONObject getContext(Long workflowInstanceId);
}
