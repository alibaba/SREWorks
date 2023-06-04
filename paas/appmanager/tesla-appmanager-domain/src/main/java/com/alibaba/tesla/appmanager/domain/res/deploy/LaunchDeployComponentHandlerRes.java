package com.alibaba.tesla.appmanager.domain.res.deploy;

import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Component 发起部署后返回的对象
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaunchDeployComponentHandlerRes implements Serializable {

    private static final long serialVersionUID = 4654449396644363295L;

    /**
     * 部署后的 ComponentSchema 对象
     */
    private ComponentSchema componentSchema;

    /**
     * 运行后需要存储的部署属性信息
     */
    private Map<String, String> attrMap;

    /**
     * 发起部署之后的组件状态
     */
    private String status;
}
