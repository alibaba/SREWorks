package com.alibaba.tesla.appmanager.server.addon.req;

import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author qiuqiang.qq@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyAddonInstanceReq implements Serializable {

    private static final long serialVersionUID = 1620042769851136052L;

    /**
     * 命名空间 ID
     */
    private String namespaceId;

    /**
     * Addon ID
     */
    private String addonId;

    /**
     * Addon Name (namespaceId, addonId, addonVersion 下唯一)
     */
    private String addonName;

    /**
     * Addon 属性字典
     */
    private Map<String, String> addonAttrs;

    /**
     * Addon Schema
     */
    private ComponentSchema schema;
}
