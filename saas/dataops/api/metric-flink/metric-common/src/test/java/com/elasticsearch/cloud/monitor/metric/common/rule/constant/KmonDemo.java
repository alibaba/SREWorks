package com.elasticsearch.cloud.monitor.metric.common.rule.constant;

import com.taobao.kmonitor.ImmutableMetricTags;
import com.taobao.kmonitor.KMonitor;
import com.taobao.kmonitor.KMonitorFactory;
import com.taobao.kmonitor.MetricType;
import com.taobao.kmonitor.PriorityType;
import com.taobao.kmonitor.impl.KMonitorConfig;

/**
 * @author xiaoping
 * @date 2019/11/28
 */
public class KmonDemo {
    private static String TEST_METRIC = "kmonitor.without.hadoop";
    private static String TEST_METRIC_PERFORMANCE_QPS = "kmonitor.performance.qps";
    private static String TEST_METRIC_PERFORMANCE_COUNT = "count.test";
    private static String TEST_PRIORITY_METRIC = "kmonitor.with.priority";
    private static String TEST_GAUGE_METRIC = "kmonitor.gauge.test";

    public static void main(String args[]) throws InterruptedException {
        KMonitorConfig.setKMonitorServiceName("client_service");
        KMonitorConfig.setKMonitorTenantName("default");
        //only for linux
        KMonitorConfig.enableSystemMetrics();
        KMonitorConfig.setSystemMetricsReportPeriod(10);
        KMonitorConfig.setSinkAddress("10.101.72.48:4141");
        //only for ali os
        //KMonitorConfig.setSystemAlimonitorCpuEnabled(true);
        //KMonitorConfig.setSystemAlimonitorLoadEnabled(true);

        KMonitor kMonitor = KMonitorFactory.getKMonitor("example");
        KMonitorFactory.start();
        kMonitor.register(TEST_METRIC, MetricType.QPS);
        kMonitor.register(TEST_METRIC_PERFORMANCE_COUNT, MetricType.COUNTER);
        kMonitor.register(TEST_METRIC_PERFORMANCE_QPS, MetricType.QPS);
        kMonitor.register(TEST_PRIORITY_METRIC, MetricType.QPS, PriorityType.CRITICAL);
        kMonitor.register(TEST_GAUGE_METRIC, MetricType.GAUGE, 126);
        KMonitorFactory.addGlobalTags(
            new ImmutableMetricTags("k", "v", KMonitorConfig.KMONITOR_SERVICE_TAG,
                "global_service"));
        KMonitorFactory.addSystemTags(new ImmutableMetricTags("ks", "vs"));
        int tick = 1;
        while (true) {
            kMonitor.report(TEST_GAUGE_METRIC, 1);
            if (tick++ % 100 == 0) {
                KMonitorFactory.delGlobalTags(new ImmutableMetricTags("k", "v"));
            }
            Thread.sleep(1000);
        }
    }
}
