package com.alibaba.tesla.appmanager.workflow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.WorkflowConstant;
import com.alibaba.tesla.appmanager.common.enums.WorkflowInstanceEventEnum;
import com.alibaba.tesla.appmanager.common.enums.WorkflowInstanceStateEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.option.WorkflowInstanceOption;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.appmanager.workflow.event.WorkflowInstanceEvent;
import com.alibaba.tesla.appmanager.workflow.repository.WorkflowInstanceRepository;
import com.alibaba.tesla.appmanager.workflow.repository.condition.WorkflowInstanceQueryCondition;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDO;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private WorkflowInstanceRepository workflowInstanceRepository;

    /**
     * 根据 Workflow 实例 ID 获取对应的 Workflow 实例
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param withExt            是否包含扩展信息
     * @return Workflow 实例对象，不存在则返回 null
     */
    @Override
    public WorkflowInstanceDO get(Long workflowInstanceId, boolean withExt) {
        return null;
    }

    /**
     * 根据条件过滤 Workflow 实例列表
     *
     * @param condition 过滤条件
     * @param withExt   是否包含扩展信息
     * @return List
     */
    @Override
    public Pagination<WorkflowInstanceDO> list(WorkflowInstanceQueryCondition condition, boolean withExt) {
        return null;
    }

    /**
     * 启动一个 Workflow 实例
     *
     * @param appId         应用 ID
     * @param configuration 启动配置
     * @param options       Workflow 配置项
     * @param creator       创建者
     * @return 启动后的 Workflow 实例
     */
    @Override
    public WorkflowInstanceDO launch(
            String appId, String configuration, WorkflowInstanceOption options, String creator) {
        String configurationSha256 = DigestUtils.sha256Hex(configuration);
        DeployAppSchema configurationSchema = SchemaUtil.toSchema(DeployAppSchema.class, configuration);
        enrichConfigurationSchema(configurationSchema);
        WorkflowInstanceDO record = WorkflowInstanceDO.builder()
                .appId(appId)
                .workflowStatus(WorkflowInstanceStateEnum.PENDING.toString())
                .workflowConfiguration(SchemaUtil.toYamlMapStr(configurationSchema))
                .workflowSha256(configurationSha256)
                .workflowOptions(JSONObject.toJSONString(options))
                .workflowCreator(options.getCreator())
                .build();
        workflowInstanceRepository.insert(record);
        Long workflowInstanceId = record.getId();
        log.info("action=createWorkflowInstance|workflowInstanceId={}|appId={}|creator={}|sha256={}|configuration={}",
                workflowInstanceId, appId, creator, configurationSha256, configuration);

        // 事件发送
        WorkflowInstanceEvent event = new WorkflowInstanceEvent(this,
                WorkflowInstanceEventEnum.START, workflowInstanceId);
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
        log.info("action=updateWorkflowInstance|workflowInstanceId={}|appId={}|status={}|creator={}", workflow.getId(),
                workflow.getAppId(), workflow.getWorkflowStatus(), workflow.getWorkflowCreator());
        int count = workflowInstanceRepository.updateByPrimaryKey(workflow);
        if (count == 0) {
            throw new AppException(AppErrorCode.LOCKER_VERSION_EXPIRED);
        }
    }

    /**
     * 触发指定 Workflow 实例的下一个内部任务的执行
     * <p>
     * 该方法用于 WorkflowInstanceTask 在以 SUCCESS 完成时触发下一个 Task 的执行，并更新 Workflow 实例状态
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param currentTaskId      workflowInstanceId 下的目前已经执行 SUCCESS 的 taskId
     * @return 更新状态后的 Workflow 实例
     */
    @Override
    public WorkflowInstanceDO triggerNextTask(Long workflowInstanceId, String currentTaskId) {
        return null;
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
     * 恢复处于 SUSPEND 状态的 Workflow 实例
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @return 更新状态后的 Workflow 实例
     */
    @Override
    public WorkflowInstanceDO resume(Long workflowInstanceId) {
        return null;
    }

    /**
     * 终止当前 Workflow 实例，并下发 InterruptedException 到 Task 侧
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @return 更新状态后的 Workflow 实例
     */
    @Override
    public WorkflowInstanceDO terminate(Long workflowInstanceId) {
        return null;
    }

    /**
     * 重试当前已经到达终态的 Workflow 实例 (SUCCESS/FAILURE/EXCEPTION/TERMINATED)
     * <p>
     * 注意该方法将会从第一个节点开始，使用原始参数重新运行一遍当前 Workflow 实例
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @return 更新状态后的 Workflow 实例
     */
    @Override
    public WorkflowInstanceDO retry(Long workflowInstanceId) {
        return null;
    }

    /**
     * 重试当前已经到达终态的 Workflow 实例 (SUCCESS/FAILURE/EXCEPTION/TERMINATED)
     * <p>
     * 注意该方法从指定 taskId 开始进行重试，即重新运行 taskId 及之后的所有 WorkflowInstance 任务
     * <p>
     * 该方法会获取 taskId 对应的快照内容，以此为输入进行重试
     *
     * @param workflowInstanceId Workflow 实例 ID
     * @param taskId             Workflow Instance Task ID
     * @return 更新状态后的 Workflow 实例
     */
    @Override
    public WorkflowInstanceDO retryFromTask(Long workflowInstanceId, String taskId) {
        return null;
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
                            .stage(WorkflowConstant.DEFAULT_WORKFLOW_STAGE.toString())
                            .properties(new JSONObject())
                            .build()))
                    .build();
            configurationSchema.getSpec().setWorkflow(defaultWorkflow);
        }
    }
}
