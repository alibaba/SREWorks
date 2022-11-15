package com.alibaba.tesla.appmanager.domain.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppComponentDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 是否为兼容记录 (true: 历史上的兼容记录, false: 通用记录)
     */
    private boolean compatible;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 最后修改时间
     */
    private Date gmtModified;

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
     * 组件对应的当前应用插件版本
     */
    private String pluginVersion;

    /**
     * 配置内容
     */
    private JSONObject config;

    /**
     * 自动生成: 当前组件对应的类型 ID
     */
    private String typeId;
}
