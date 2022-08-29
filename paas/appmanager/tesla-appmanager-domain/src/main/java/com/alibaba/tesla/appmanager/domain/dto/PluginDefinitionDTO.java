package com.alibaba.tesla.appmanager.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Plugin Definition DTO
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginDefinitionDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 最后修改时间
     */
    private Date gmtModified;

    /**
     * Plugin 类型
     */
    private String pluginKind;

    /**
     * Plugin 唯一标识
     */
    private String pluginName;

    /**
     * Plugin 版本
     */
    private String pluginVersion;

    /**
     * 是否已安装注册
     */
    private Boolean pluginRegistered;

    /**
     * Plugin 包路径
     */
    private String packagePath;

    /**
     * Plugin 描述
     */
    private String pluginDescription;

    /**
     * Plugin 依赖 (JSON Array)
     */
    private String pluginDependencies;

    /**
     * Plugin Schema
     */
    private String pluginSchema;
}
