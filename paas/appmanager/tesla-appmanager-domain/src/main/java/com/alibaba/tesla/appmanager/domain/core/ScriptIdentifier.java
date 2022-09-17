package com.alibaba.tesla.appmanager.domain.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Groovy 脚本标识
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScriptIdentifier {

    /**
     * 类型
     */
    private String kind;

    /**
     * 名称
     */
    private String name;

    /**
     * 版本
     */
    private Integer revision;
}
