package com.alibaba.tesla.appmanager.workflow.controller;

import com.alibaba.tesla.appmanager.api.provider.WorkflowInstanceProvider;
import com.alibaba.tesla.appmanager.api.provider.WorkflowTaskProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.WorkflowInstanceDTO;
import com.alibaba.tesla.appmanager.domain.dto.WorkflowTaskDTO;
import com.alibaba.tesla.appmanager.domain.option.WorkflowInstanceOption;
import com.alibaba.tesla.appmanager.domain.req.workflow.*;
import com.alibaba.tesla.appmanager.domain.res.workflow.WorkflowInstanceOperationRes;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Workflow Instance Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Tag(name = "工作流 API")
@RequestMapping("/workflow")
@RestController
public class WorkflowController extends AppManagerBaseController {

    @Autowired
    private WorkflowInstanceProvider workflowInstanceProvider;

    @Autowired
    private WorkflowTaskProvider workflowTaskProvider;

    @Operation(summary = "发起 Workflow")
    @PostMapping(value = "/launch")
    @ResponseBody
    public TeslaBaseResult launch(
            @RequestParam("appId") String appId,
            @RequestParam("category") String category,
            @RequestBody String body, OAuth2Authentication auth) {
        WorkflowInstanceOption options = WorkflowInstanceOption.builder()
                .category(category)
                .creator(getOperator(auth))
                .build();
        try {
            WorkflowInstanceDTO response = workflowInstanceProvider.launch(appId, body, options);
            return buildSucceedResult(response);
        } catch (Exception e) {
            log.error("cannot launch deployments|exception={}|request={}", ExceptionUtils.getStackTrace(e), body);
            return buildExceptionResult(e);
        }
    }

    @Operation(summary = "查询 Workflow Instance 列表")
    @GetMapping
    @ResponseBody
    public TeslaBaseResult list(
            @ModelAttribute WorkflowInstanceListReq request, OAuth2Authentication auth
    ) throws Exception {
        Pagination<WorkflowInstanceDTO> response = workflowInstanceProvider.list(request);
        return buildSucceedResult(response);
    }

    @Operation(summary = "查询 Workflow Instance 详情")
    @GetMapping("{instanceId}")
    @ResponseBody
    public TeslaBaseResult get(
            @PathVariable("instanceId") Long instanceId, OAuth2Authentication auth
    ) throws Exception {
        WorkflowInstanceDTO response = workflowInstanceProvider.get(instanceId, true);
        return buildSucceedResult(response);
    }

    @Operation(summary = "设置 Workflow Instance Context", description = "用于覆写 Workflow 实例中的 Context")
    @PutMapping("{instanceId}/context")
    @ResponseBody
    public TeslaBaseResult putContext(
            @PathVariable("instanceId") Long instanceId,
            @RequestBody WorkflowPutContextReq request,
            OAuth2Authentication auth
    ) throws Exception {
        workflowInstanceProvider.putContext(instanceId, request.getContext());
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }

    @Operation(summary = "唤醒 Workflow Instance", description = "用于唤醒处于 SUSPEND 状态的 Workflow 实例")
    @PutMapping("{instanceId}/resume")
    @ResponseBody
    public TeslaBaseResult resume(
            @PathVariable("instanceId") Long instanceId, OAuth2Authentication auth
    ) throws Exception {
        WorkflowInstanceOperationRes response = workflowInstanceProvider.resume(instanceId);
        return buildSucceedResult(response);
    }

    @Operation(summary = "终止 Workflow Instance",
            description = "终止当前 Workflow 实例，并下发 InterruptedException 到 Task 侧")
    @PutMapping("{instanceId}/terminate")
    @ResponseBody
    public TeslaBaseResult terminate(
            @PathVariable("instanceId") Long instanceId, OAuth2Authentication auth
    ) throws Exception {
        WorkflowInstanceOperationRes response = workflowInstanceProvider.terminate(instanceId);
        return buildSucceedResult(response);
    }

    @Operation(summary = "重试 Workflow Instance",
            description = "重试当前已经到达终态的 Workflow 实例 (SUCCESS/FAILURE/EXCEPTION/TERMINATED)，" +
                    "注意该方法将会从第一个节点开始，使用原始参数重新运行一遍当前 Workflow 实例")
    @PutMapping("{instanceId}/retry")
    @ResponseBody
    public TeslaBaseResult retry(
            @PathVariable("instanceId") Long instanceId, OAuth2Authentication auth
    ) throws Exception {
        WorkflowInstanceOperationRes response = workflowInstanceProvider.retry(instanceId);
        return buildSucceedResult(response);
    }

    @Operation(summary = "重试 Workflow Instance (自定义起始 Task)",
            description = "重试当前已经到达终态的 Workflow 实例 (SUCCESS/FAILURE/EXCEPTION/TERMINATED)，" +
                    "注意该方法从指定 taskId 开始进行重试，即重新运行 taskId 及之后的所有 WorkflowInstance 任务，" +
                    "该方法会获取 taskId 对应的快照内容，以此为输入进行重试")
    @PutMapping("{instanceId}/retryFromTask")
    @ResponseBody
    public TeslaBaseResult retryFromTask(
            @PathVariable("instanceId") Long instanceId,
            @RequestParam("taskId") Long taskId,
            OAuth2Authentication auth
    ) throws Exception {
        WorkflowInstanceDTO response = workflowInstanceProvider.retryFromTask(instanceId, taskId);
        return buildSucceedResult(response);
    }

    @Operation(summary = "查询 Workflow Task 列表")
    @GetMapping("{instanceId}/tasks")
    @ResponseBody
    public TeslaBaseResult listTask(
            @PathVariable("instanceId") Long instanceId,
            @ModelAttribute WorkflowTaskListReq request, OAuth2Authentication auth
    ) throws Exception {
        request.setInstanceId(instanceId);
        Pagination<WorkflowTaskDTO> response = workflowTaskProvider.list(request);
        for (int i = 0; i < response.getItems().size(); i++) {
            WorkflowTaskDTO current = response.getItems().get(i);
            current.setBatchId((long) (i + 1));
            if (StringUtils.isEmpty(current.getDeployAppUnitId())) {
                current.setDeployAppUnitId("internal");
            }
        }
        return buildSucceedResult(response);
    }

    @Operation(summary = "查询 Workflow Task 详情")
    @GetMapping("{instanceId}/tasks/{taskId}")
    @ResponseBody
    public TeslaBaseResult getTask(
            @PathVariable("instanceId") Long instanceId,
            @PathVariable("taskId") Long taskId,
            OAuth2Authentication auth
    ) throws Exception {
        WorkflowTaskDTO response = workflowTaskProvider.get(taskId, true);
        return buildSucceedResult(response);
    }

    @Operation(summary = "终止 Workflow Task",
            description = "终止指定 Workflow 任务 (x -> TERMINATED)")
    @PutMapping("{instanceId}/tasks/{taskId}/terminate")
    @ResponseBody
    public TeslaBaseResult terminateTask(
            @PathVariable("instanceId") Long instanceId,
            @PathVariable("taskId") Long taskId,
            @RequestBody WorkflowTaskTerminateReq request,
            OAuth2Authentication auth
    ) throws Exception {
        workflowTaskProvider.terminate(taskId, request.getExtMessage());
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }

    @Operation(summary = "暂停 Workflow Task",
            description = "暂停指定 Workflow 任务 (RUNNING -> RUNNING_SUSPEND / WAITING -> WAITING_SUSPEND)")
    @PutMapping("{instanceId}/tasks/{taskId}/suspend")
    @ResponseBody
    public TeslaBaseResult suspendTask(
            @PathVariable("instanceId") Long instanceId,
            @PathVariable("taskId") Long taskId,
            @RequestBody WorkflowTaskSuspendReq request,
            OAuth2Authentication auth
    ) throws Exception {
        workflowTaskProvider.suspend(taskId, request.getExtMessage());
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }
}