package com.alibaba.tesla.appmanager.domain.req.plugin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginGetReq {

    private String pluginName;

    private String pluginVersion;

}
