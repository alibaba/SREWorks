package com.alibaba.tesla.appmanager.domain.req.appversion;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用关联版本创建请求
 *
 * @author jiongen.zje@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionCreateReq {

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
