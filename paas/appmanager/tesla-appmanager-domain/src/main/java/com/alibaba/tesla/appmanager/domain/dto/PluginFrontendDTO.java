package com.alibaba.tesla.appmanager.domain.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Plugin Frontend 资源 DTO
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginFrontendDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * Plugin 唯一标识
     */
    private String pluginName;

    /**
     * Plugin 版本 (SemVer)
     */
    private String pluginVersion;

    /**
     * 名称
     */
    private String name;

    /**
     * 页面配置
     */
    private JSONObject config;
}
