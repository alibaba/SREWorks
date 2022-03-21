package com.alibaba.tesla.appmanager.domain.req.apppackage;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建 Component Package 请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppPackageTaskCreateReq {

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 包版本
     */
    private String version;

    /**
     * 当前 AppPackage 的标签列表
     */
    private List<String> tags;

    /**
     * 当前 AppPackage 引用的 components 列表
     */
    private List<ComponentBinder> components;

    /**
     * 是否保存当前系统中的 Deploy Config 到组件包中
     */
    private boolean storeConfiguration = true;

    private boolean develop;
}
