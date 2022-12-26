package com.alibaba.tesla.appmanager.workflow.repository.domain;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Workflow 任务表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowTaskDO {
    /**
     * ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 最后修改时间
     */
    private Date gmtModified;

    /**
     * Workflow 实例 ID
     */
    private Long workflowInstanceId;

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 开始时间
     */
    private Date gmtStart;

    /**
     * 结束时间
     */
    private Date gmtEnd;

    /**
     * Workflow 任务类型
     */
    private String taskType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * Workflow 任务节点运行阶段 (pre-render, post-render, post-deploy)
     */
    private String taskStage;

    /**
     * Workflow 任务节点状态 (PENDING, RUNNING, WAITING[完成UserFunction后等待完成], SUSPEND, SUCCESS, FAILURE, EXCEPTION, TERMINATED)
     */
    private String taskStatus;

    /**
     * 发起的新 DeployApp ID
     */
    private Long deployAppId;

    /**
     * 乐观锁版本
     */
    private Integer lockVersion;

    /**
     * 发起的新 DeployApp ID 归属单元
     */
    private String deployAppUnitId;

    /**
     * 发起的新 DeployApp ID 归属 Namespace
     */
    private String deployAppNamespaceId;

    /**
     * 发起的新 DeployApp ID 归属 Stage
     */
    private String deployAppStageId;

    /**
     * 发起的新 WorkflowInstance ID
     */
    private Long deployWorkflowInstanceId;

    /**
     * Workflow 任务节点属性 (JSONObject 字符串)
     */
    private String taskProperties;

    /**
     * Workflow 任务节点执行出错信息 (仅 task_status==EXCEPTIOIN 下存在)
     */
    private String taskErrorMessage;

    /**
     * Workflow 任务节点输出 (JSONArray)
     */
    private String taskOutputs;

    /**
     * Workflow 任务节点输入 (JSONArray)
     */
    private String taskInputs;
}