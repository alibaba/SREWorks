package com.alibaba.tesla.appmanager.domain.req;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppMetaCreateReq {

    /**
     * 应用唯一标识
     */
    private String appId;

    /**
     * 应用 Options
     */
    private JSONObject options;

    /**
     * 更新模式，可选 append(追加) / overwrite(覆盖)
     */
    private String mode = "append";
}
