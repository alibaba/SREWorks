package com.alibaba.tesla.appmanager.domain.container;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用绑定的组件的定位容器
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppComponentLocationContainer {

    /**
     * 组件类型
     */
    private String componentType;

    /**
     * 组件名称
     */
    private String componentName;
}
