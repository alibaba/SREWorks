package com.alibaba.tesla.appmanager.workflow.service;

import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.req.CreateWorkflowSnapshotReq;
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
     * 根据条件过滤 Workflow 任务列表
     *
     * @param condition 过滤条件
     * @return List of WorkflowSnapshot
     */
    Pagination<WorkflowSnapshotDO> list(WorkflowSnapshotQueryCondition condition);

    /**
     * 创建一个 Workflow 快照
     *
     * @param request 创建 Workflow 快照请求
     * @return 创建后的 WorkflowSnapshot 对象
     */
    WorkflowSnapshotDO create(CreateWorkflowSnapshotReq request);

    /**
     * 根据条件删除 Workflow 快照
     *
     * @param request 删除 Workflow 快照请求
     * @return 删除数量
     */
    int delete(DeleteWorkflowSnapshotReq request);
}
