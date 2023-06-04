package com.alibaba.tesla.appmanager.domain.req.appversion;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用关联组件更新请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionUpdateReq {


    /**
     * 应用版本 ID
     */
    private Long id;

    /**
     * 应用 ID
     */
    private String appId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 版本标签
     */
    private String versionLabel;

    /**
     * 版本属性
     */
    private JSONObject versionProperties;


}
