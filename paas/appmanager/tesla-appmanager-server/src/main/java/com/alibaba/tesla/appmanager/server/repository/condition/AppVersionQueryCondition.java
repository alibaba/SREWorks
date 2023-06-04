package com.alibaba.tesla.appmanager.server.repository.condition;

import com.alibaba.tesla.appmanager.common.BaseCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 应用版本查询条件类
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionQueryCondition extends BaseCondition {

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 版本
     */
    private String version;

    /**
     * 版本标签
     */
    private String versionLabel;
}
