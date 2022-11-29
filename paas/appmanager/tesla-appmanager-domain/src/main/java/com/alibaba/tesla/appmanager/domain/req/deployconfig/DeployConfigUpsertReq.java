package com.alibaba.tesla.appmanager.domain.req.deployconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployConfigUpsertReq {

    /**
     * API 版本
     */
    private String apiVersion;

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 类型 ID
     */
    private String typeId;

    /**
     * 环境 ID
     */
    private String envId;

    /**
     * 配置信息 (Yaml)
     */
    private String config;

    /**
     * 配置信息 (JSONArray), 如果传递了当前值，则 config 无效
     */
    private JSONArray configJsonArray;

    /**
     * 配置信息 (JSONObject), 如果传递了当前值，则 config 无效
     */
    private JSONObject configJsonObject;

    /**
     * 是否继承
     */
    private boolean inherit;

    /**
     * Namespace ID
     */
    private String isolateNamespaceId;

    /**
     * Stage ID
     */
    private String isolateStageId;

    /**
     * 归属产品 ID
     */
    private String productId;

    /**
     * 归属发布版本 ID
     */
    private String releaseId;
}
