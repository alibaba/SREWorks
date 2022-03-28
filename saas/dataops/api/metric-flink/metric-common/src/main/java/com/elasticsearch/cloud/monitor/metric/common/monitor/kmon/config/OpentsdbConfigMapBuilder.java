package com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config;

import com.google.common.collect.Maps;

import java.util.Map;

import static com.elasticsearch.cloud.monitor.metric.common.monitor.opentsdb.OpentsdbSink.CONFIG_ENDPOINT_KEY;
import static com.elasticsearch.cloud.monitor.metric.common.monitor.opentsdb.OpentsdbSink.CONFIG_REPORT_SWITCH_KEY;

/**
 * @author xiaoping
 * @date 2019/12/2
 */
public class OpentsdbConfigMapBuilder {
    private Map<String, String> sinkConfMap = Maps.newHashMap();

    public OpentsdbConfigMapBuilder reportEnable(boolean enable) {
        sinkConfMap.put(CONFIG_REPORT_SWITCH_KEY, String.valueOf(enable));
        return this;
    }

    public OpentsdbConfigMapBuilder endpoint(String endpoint) {
        sinkConfMap.put(CONFIG_ENDPOINT_KEY, endpoint);
        return this;
    }

    public Map<String, String> build() {
        return sinkConfMap;
    }

}
