package com.alibaba.tesla.appmanager.domain.req.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTaskTerminateReq implements Serializable {

    private Long workflowTaskId;

    private String extMessage;
}
