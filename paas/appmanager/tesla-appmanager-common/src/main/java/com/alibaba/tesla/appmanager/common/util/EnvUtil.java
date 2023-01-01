package com.alibaba.tesla.appmanager.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 环境工具类
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
public class EnvUtil {

    /**
     * 通过 unitId/clusterId/namespaceId/stageId 产出 deploy config 中使用的 envId
     *
     * @param unitId      单元 ID
     * @param clusterId   集群 ID
     * @param namespaceId Namespace ID
     * @param stageId     Stage ID
     * @return Env ID
     */
    public static String generate(String unitId, String clusterId, String namespaceId, String stageId) {
        List<String> arr = new ArrayList<>();
        if (StringUtils.isNotEmpty(unitId)) {
            arr.add(String.format("Unit:%s", unitId));
        }
        if (StringUtils.isNotEmpty(clusterId)) {
            arr.add(String.format("Cluster:%s", clusterId));
        }
        if (StringUtils.isNotEmpty(namespaceId)) {
            arr.add(String.format("Namespace:%s", namespaceId));
        }
        if (StringUtils.isNotEmpty(stageId)) {
            arr.add(String.format("Stage:%s", stageId));
        }
        if (arr.size() == 0) {
            return "";
        }
        return String.join("::", arr);
    }

    /**
     * 返回当前是否为 SREWORKS 环境
     *
     * @return true or false
     */
    public static boolean isSreworks() {
        return "sreworks".equals(System.getenv("K8S_NAMESPACE"));
    }

    /**
     * 返回当前系统的默认 Namespace ID
     *
     * @return string
     */
    public static String defaultNamespaceId() {
        String result = System.getenv("DEFAULT_NAMESPACE_ID");
        if (StringUtils.isEmpty(result)) {
            if (isSreworks()) {
                return "sreworks";
            } else {
                return "default";
            }
        }
        return result;
    }

    /**
     * 返回当前系统的默认 Stage ID
     *
     * @return string
     */
    public static String defaultStageId() {
        String result = System.getenv("DEFAULT_STAGE_ID");
        if (StringUtils.isEmpty(result)) {
            if (isSreworks()) {
                return "dev";
            } else {
                return "pre";
            }
        }
        return result;
    }
}
