package com.alibaba.tesla.appmanager.domain.req.plugin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginUploadReq {

    /**
     * 是否强制覆盖
     */
    private boolean overwrite;

    /**
     * 是否默认开启
     */
    private boolean enable;
}
