package com.alibaba.tesla.appmanager.server.repository.domain;

import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 应用组件关联表
 *
 * @author qianmo.zm@alibaba-inc.com
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppAddonDO {

    /**
     * 主键
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
     * 应用唯一标识
     */
    private String appId;

    /**
     * 类型（可选 CORE-SERVICE / THIRDPARTY）
     */
    private ComponentTypeEnum addonType;

    /**
     * 附加组件唯一标识
     */
    private String addonId;

    /**
     * 版本号
     */
    private String addonVersion;

    /**
     * 附加组件配置
     */
    private String addonConfig;

    /**
     * 名称
     */
    private String name;
}