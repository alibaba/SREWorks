package com.elasticsearch.cloud.monitor.metric.common.monitor.opentsdb;

import com.google.common.collect.Lists;
import com.taobao.kmonitor.core.MetricsRecord;
import com.taobao.kmonitor.core.MetricsTag;
import com.taobao.kmonitor.core.MetricsValue;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.SubsetConfiguration;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaoping
 * @date 2019/12/20
 */
public class OpentsdbSinkTest {
    private OpentsdbSink opentsdbSink;

    @Before
    public void setUp() throws Exception {
        SubsetConfiguration subsetConfiguration = new SubsetConfiguration(new BaseConfiguration(), "");
        subsetConfiguration.setProperty("report_switch", true);
        subsetConfiguration.setProperty("endpoint", "http://123.57.72.254:3390");
        opentsdbSink = new OpentsdbSink();
        opentsdbSink.init(subsetConfiguration);
    }

    @Test
    public void putMetrics() {
        MetricsRecord metricsRecord = new MetricsRecord(null, System.currentTimeMillis(),
            Lists.newArrayList(new MetricsTag("xiaopinghost", "1244"), new MetricsTag("env", "beijing")),
            Lists.newArrayList(new MetricsValue("xiaopingcpu", 12), new MetricsValue("memory", 2.0)));

        opentsdbSink.putMetrics(metricsRecord);
    }
}
