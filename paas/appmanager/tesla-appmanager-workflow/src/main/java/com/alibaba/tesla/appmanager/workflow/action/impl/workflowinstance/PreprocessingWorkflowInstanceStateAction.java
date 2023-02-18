package com.alibaba.tesla.appmanager.workflow.action.impl.workflowinstance;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.enums.*;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.container.WorkflowGraphNodeId;
import com.alibaba.tesla.appmanager.domain.option.WorkflowInstanceOption;
import com.alibaba.tesla.appmanager.domain.req.UpdateWorkflowSnapshotReq;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.appmanager.domain.schema.WorkflowGraph;
import com.alibaba.tesla.appmanager.dynamicscript.core.GroovyHandlerFactory;
import com.alibaba.tesla.appmanager.workflow.action.WorkflowInstanceStateAction;
import com.alibaba.tesla.appmanager.workflow.event.WorkflowInstanceEvent;
import com.alibaba.tesla.appmanager.workflow.event.WorkflowTaskEvent;
import com.alibaba.tesla.appmanager.workflow.event.loader.WorkflowInstanceStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowSnapshotDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowSnapshotService;
import com.alibaba.tesla.appmanager.workflow.service.WorkflowTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service("PreprocessingWorkflowInstanceStateAction")
public class PreprocessingWorkflowInstanceStateAction implements WorkflowInstanceStateAction, ApplicationRunner {

    private static final WorkflowInstanceStateEnum STATE = WorkflowInstanceStateEnum.PREPROCESSING;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private GroovyHandlerFactory groovyHandlerFactory;

    @Autowired
    private WorkflowTaskService workflowTaskService;

    @Autowired
    private WorkflowSnapshotService workflowSnapshotService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        publisher.publishEvent(new WorkflowInstanceStateActionLoadedEvent(
                this, STATE.toString(), this.getClass().getSimpleName()));
    }

    /**
     * 自身处理逻辑
     *
     * @param instance Workflow 实例
     */
    @Override
    public void run(WorkflowInstanceDO instance) {
        Long workflowInstanceId = instance.getId();
        String appId = instance.getAppId();
        try {
            DeployAppSchema configuration = SchemaUtil.toSchema(
                    DeployAppSchema.class, instance.getWorkflowConfiguration());
            WorkflowInstanceOption options = JSONObject.parseObject(
                    instance.getWorkflowOptions(), WorkflowInstanceOption.class);
            log.info("action=preprocessingWorkflowInstance|appId={}|workflowInstanceId={}", appId, workflowInstanceId);

            // 创建 tasks
            WorkflowGraph graph = JSONObject.parseObject(instance.getWorkflowGraph(), WorkflowGraph.class);
            List<String> entryNodes = graph.getZeroDegreeNodes();
            List<WorkflowTaskDO> tasks = createWorkflowTasks(instance, configuration);
            log.info("all workflow tasks has created, prepare to run entry tasks|appId={}|workflowInstanceId={}",
                    appId, workflowInstanceId);
            for (WorkflowTaskDO task : tasks) {
                // 创建一个空的快照到当前 task 上
                JSONObject context = new JSONObject();
                if (options.getInitContext() != null) {
                    context.putAll(options.getInitContext());
                }
                WorkflowSnapshotDO snapshot = workflowSnapshotService.update(UpdateWorkflowSnapshotReq.builder()
                        .workflowInstanceId(task.getWorkflowInstanceId())
                        .workflowTaskId(task.getId())
                        .context(context)
                        .build());
                log.info("workflow snapshot has created|workflowInstanceId={}|workflowTaskId={}|workflowSnapshotId={}|" +
                                "context={}", snapshot.getWorkflowInstanceId(), snapshot.getWorkflowTaskId(),
                        snapshot.getId(), snapshot.getSnapshotContext());

                // 如果当前 task 可以没有前置节点，直接触发执行
                String nodeId = (new WorkflowGraphNodeId(task.getTaskType(), task.getTaskName())).toString();
                if (!entryNodes.contains(nodeId)) {
                    continue;
                }
                WorkflowTaskEvent event = new WorkflowTaskEvent(this, WorkflowTaskEventEnum.START, task);
                publisher.publishEvent(event);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                    String.format("unknown error processing workflow instance|workflowInstanceId=%d|appId=%s|" +
                            "workflowOptions=%s|creator=%s|exception=%s", workflowInstanceId, appId,
                            instance.getWorkflowOptions(), instance.getWorkflowCreator(),
                            ExceptionUtils.getStackTrace(e)));
        }

        // 触发自身流转到下一状态
        publisher.publishEvent(new WorkflowInstanceEvent(this,
                WorkflowInstanceEventEnum.PREPROCESS_FINISHED, instance));
    }

    /**
     * 针对每一个 execute order 创建一个 workflow task
     *
     * @param instance      Workflow Instance 数据库对象
     * @param configuration Workflow Configuration
     * @return WorkflowTask 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<WorkflowTaskDO> createWorkflowTasks(WorkflowInstanceDO instance, DeployAppSchema configuration) {
        Long workflowInstanceId = instance.getId();
        List<DeployAppSchema.WorkflowStep> workflowSteps = configuration.getSpec().getWorkflow().getSteps();
        List<WorkflowTaskDO> tasks = new ArrayList<>();
        Set<String> usedSet = new HashSet<>();
        for (int index = 0; index < workflowSteps.size(); index++) {
            DeployAppSchema.WorkflowStep currentStep = workflowSteps.get(index);
            String taskType = currentStep.getType();
            String taskName = currentStep.getName();
            WorkflowGraphNodeId nodeId = new WorkflowGraphNodeId(taskType, taskName);
            if (usedSet.contains(nodeId.toString())) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, String.format("duplicate workflow step name " +
                        "in the same workflow type|step=%s", JSONObject.toJSONString(currentStep)));
            }
            usedSet.add(nodeId.toString());
            checkWorkflowTypeExists(taskType);
            WorkflowStageEnum workflowStage = WorkflowStageEnum.fromString(currentStep.getStage());
            JSONObject workflowProperties = currentStep.getProperties();
            List<DeployAppSchema.WorkflowStepOutput> outputs = currentStep.getOutputs();
            List<DeployAppSchema.WorkflowStepInput> inputs = currentStep.getInputs();

            // 根据 workflow stage 进行前置渲染
            switch (workflowStage) {
                case PRE_RENDER:
                    // TODO
                    break;
                case POST_RENDER:
                    // TODO
                    break;
                case POST_DEPLOY:
                    // TODO
                    break;
                default:
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("invalid workflow stage %s", currentStep.getStage()));
            }

            // 创建 workflow task
            String taskStatus = WorkflowTaskStateEnum.PENDING.toString();
            WorkflowTaskDO record = WorkflowTaskDO.builder()
                    .workflowInstanceId(workflowInstanceId)
                    .appId(instance.getAppId())
                    .taskType(taskType)
                    .taskName(taskName)
                    .taskStage(workflowStage.toString())
                    .taskProperties(JSONObject.toJSONString(workflowProperties))
                    .taskStatus(taskStatus)
                    .taskInputs(JSONArray.toJSONString(inputs))
                    .taskOutputs(JSONArray.toJSONString(outputs))
                    .build();
            WorkflowTaskDO task = workflowTaskService.create(record);
            log.info("action=createWorkflowTask|workflowInstanceId={}|workflowTaskId={}|appId={}|taskType={}|" +
                            "taskName={}|taskStage={}|taskStatus={}", workflowInstanceId, task.getId(),
                    instance.getAppId(), taskType, taskName, workflowStage, taskStatus);
            tasks.add(task);
        }
        return tasks;
    }

    /**
     * 检测 Workflow Type 是否存在且可用
     *
     * @param workflowType Workflow 类型
     */
    private void checkWorkflowTypeExists(String workflowType) {
        if (!groovyHandlerFactory.exists(DynamicScriptKindEnum.WORKFLOW.toString(), workflowType)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("workflow type %s is not exists", workflowType));
        }
    }
}
