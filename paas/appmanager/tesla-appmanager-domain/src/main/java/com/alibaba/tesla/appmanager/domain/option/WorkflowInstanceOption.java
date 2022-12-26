package com.alibaba.tesla.appmanager.domain.option;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.enums.WorkflowExecuteModeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstanceOption {

    /**
     * Workflow 分类
     */
    private String category;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 初始化上下文
     */
    private JSONObject initContext;

    /**
     * 运行模式 (STEP_BY_STEP / DAG)
     */
    private WorkflowExecuteModeEnum mode;
}
