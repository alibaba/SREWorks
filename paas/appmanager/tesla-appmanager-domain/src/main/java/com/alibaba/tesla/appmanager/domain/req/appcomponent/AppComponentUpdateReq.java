package com.alibaba.tesla.appmanager.domain.req.appcomponent;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用关联组件更新请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppComponentUpdateReq {

    /**
     * 应用组件绑定 ID
     */
    private Long id;

    /**
     * Namespace ID
     */
    private String namespaceId;

    /**
     * Stage ID
     */
    private String stageId;

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 分类
     */
    private String category;

    /**
     * 组件类型
     */
    private String componentType;

    /**
     * 组件名称
     */
    private String componentName;

    /**
     * 配置内容
     */
    private JSONObject config;
}
