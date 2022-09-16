package com.alibaba.tesla.appmanager.server.repository.domain;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
    * 应用组件绑定表
    */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppComponentDO {
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
    * 应用 ID
    */
    private String appId;

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
    private String config;
}