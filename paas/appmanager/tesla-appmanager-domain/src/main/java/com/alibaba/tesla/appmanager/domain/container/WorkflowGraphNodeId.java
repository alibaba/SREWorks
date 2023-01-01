package com.alibaba.tesla.appmanager.domain.container;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Workflow Graph 节点 ID
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
public class WorkflowGraphNodeId {

    private static final String KEY_TYPE = "Type";
    private static final String KEY_NAME = "Name";
    private static final String KEY_VAR = "Var";

    private String type;
    private String name;
    private String var;

    public WorkflowGraphNodeId(String type, String name) {
        if (StringUtils.isAnyEmpty(type, name)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "empty type/name parameters");
        }
        this.type = type;
        this.name = name;
        this.var = "";
    }

    public WorkflowGraphNodeId(String type, String name, String var) {
        if (StringUtils.isAnyEmpty(type, name, var)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "empty type/name/var parameters");
        }
        this.type = type;
        this.name = name;
        this.var = var;
    }

    /**
     * 将 nodeId 转为 WorkflowGraphNodeId 对象
     *
     * @param nodeId nodeId 字符串
     * @return WorkflowGraphNodeId 对象
     */
    public static WorkflowGraphNodeId valueOf(String nodeId) {
        if (StringUtils.isEmpty(nodeId)) {
            return null;
        }

        String type = null, name = null, var = null;
        for (String item : nodeId.split("::")) {
            String[] arr = item.split(":");
            if (arr.length != 2) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, String.format("invalid nodeId %s", nodeId));
            }
            switch (arr[0]) {
                case KEY_TYPE:
                    type = arr[1];
                    break;
                case KEY_NAME:
                    name = arr[1];
                    break;
                case KEY_VAR:
                    var = arr[1];
                    break;
                default:
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS, String.format("invalid nodeId %s", nodeId));
            }
        }
        if (StringUtils.isAnyEmpty(type, name)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, String.format("invalid nodeId %s", nodeId));
        }
        if (StringUtils.isEmpty(var)) {
            return new WorkflowGraphNodeId(type, name);
        } else {
            return new WorkflowGraphNodeId(type, name, var);
        }
    }

    /**
     * 转换为数据库中存储的 envId
     *
     * @return string
     */
    @Override
    public String toString() {
        List<String> arr = new ArrayList<>();
        if (StringUtils.isNotEmpty(type)) {
            arr.add(String.join(":", Arrays.asList(KEY_TYPE, type)));
        }
        if (StringUtils.isNotEmpty(name)) {
            arr.add(String.join(":", Arrays.asList(KEY_NAME, name)));
        }
        if (StringUtils.isNotEmpty(var)) {
            arr.add(String.join(":", Arrays.asList(KEY_VAR, var)));
        }
        if (arr.size() > 0) {
            return String.join("::", arr);
        } else {
            return "";
        }
    }
}
