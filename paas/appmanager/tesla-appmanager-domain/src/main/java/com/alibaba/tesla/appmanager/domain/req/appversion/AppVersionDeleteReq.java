package com.alibaba.tesla.appmanager.domain.req.appversion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用关联组件删除请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionDeleteReq {

    /**
     * 应用组件绑定 ID
     */
    private Long id;
}
