package com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config;

import com.google.common.collect.Maps;

import java.util.Map;

import static com.elasticsearch.cloud.monitor.sdk.sink.SlsMonitorSink.*;

/**
 * @author xiaoping
 * @date 2019/12/2
 */
public class SlsConfigMapBuilder {
    private Map<String, String> sinkConfMap = Maps.newHashMap();

    public SlsConfigMapBuilder reportEnable(boolean enable) {
        sinkConfMap.put(CONFIG_REPORT_SWITCH, String.valueOf(enable));
        return this;
    }

    public SlsConfigMapBuilder endpoint(String endpoint) {
        sinkConfMap.put(CONFIG_END_POINT, endpoint);
        return this;
    }

    public SlsConfigMapBuilder project(String project) {
        sinkConfMap.put(CONFIG_SLS_PROJECT, project);
        return this;
    }

    public SlsConfigMapBuilder logstore(String logstore) {
        sinkConfMap.put(CONFIG_LOG_STORE, logstore);
        return this;
    }

    public SlsConfigMapBuilder accesskey(String accesskey) {
        sinkConfMap.put(CONFIG_ACCESS_KEY, accesskey);
        return this;
    }

    public SlsConfigMapBuilder accesssecret(String accesssecret) {
        sinkConfMap.put(CONFIG_ACCESS_SECRET, accesssecret);
        return this;
    }

    public Map<String, String> build() {
        return sinkConfMap;
    }

}
