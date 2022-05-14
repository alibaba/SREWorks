package com.alibaba.tesla.appmanager.domain.res.workflow;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 执行 Workflow Handler 返回结果
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteWorkflowHandlerRes implements Serializable {

    /**
     * 修改后的上下文 context 信息，如果不传递，则默认取 ExecuteWorkflowHandlerReq 中的传入值作为传出值
     */
    private JSONObject context;

    /**
     * 如果当前 workflow 触发了部署，那么此处返回部署单 ID
     */
    private Long deployAppId;
}
