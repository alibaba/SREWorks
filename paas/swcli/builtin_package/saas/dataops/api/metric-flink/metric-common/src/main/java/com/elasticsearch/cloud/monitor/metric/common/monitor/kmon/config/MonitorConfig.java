package com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xiaoping
 * @date 2019/12/2
 */
@Setter
@Getter
public class MonitorConfig {
    private SlsConfigMapBuilder slsConfigMapBuilder;
    private FlumeConfigMapBuilder flumeConfigMapBuilder;
    private OpentsdbConfigMapBuilder opentsdbConfigMapBuilder;

    public MonitorConfig(
        SlsConfigMapBuilder slsConfigMapBuilder,
        FlumeConfigMapBuilder flumeConfigMapBuilder, OpentsdbConfigMapBuilder opentsdbConfigMapBuilder) {
        this.slsConfigMapBuilder = slsConfigMapBuilder;
        this.flumeConfigMapBuilder = flumeConfigMapBuilder;
        this.opentsdbConfigMapBuilder = opentsdbConfigMapBuilder;
    }
}
