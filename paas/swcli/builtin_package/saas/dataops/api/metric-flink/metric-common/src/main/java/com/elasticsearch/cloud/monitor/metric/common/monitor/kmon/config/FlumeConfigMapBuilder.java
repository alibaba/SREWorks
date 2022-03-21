package com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config;

import java.util.Map;

import com.google.common.collect.Maps;

import static com.elasticsearch.cloud.monitor.metric.common.monitor.opentsdb.OpentsdbSink.CONFIG_REPORT_SWITCH_KEY;
import static com.taobao.kmonitor.sink.flume.FlumeSink.FLUME_ADDRESS;

/**
 * @author xiaoping
 * @date 2019/12/2
 */
public class FlumeConfigMapBuilder {
    private Map<String, String> sinkConfMap = Maps.newHashMap();

    public FlumeConfigMapBuilder address(String address) {
        sinkConfMap.put(FLUME_ADDRESS, address);
        return this;
    }

    public FlumeConfigMapBuilder assigned(Boolean assigned) {
        sinkConfMap.put("assigned", String.valueOf(assigned));
        return this;
    }

    public FlumeConfigMapBuilder enable(Boolean enable) {
        sinkConfMap.put(CONFIG_REPORT_SWITCH_KEY, String.valueOf(enable));
        return this;
    }

    public Map<String, String> build() {
        return sinkConfMap;
    }
}
