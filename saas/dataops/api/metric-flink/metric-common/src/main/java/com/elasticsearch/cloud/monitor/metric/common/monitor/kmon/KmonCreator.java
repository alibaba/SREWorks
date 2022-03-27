package com.elasticsearch.cloud.monitor.metric.common.monitor.kmon;

import com.elasticsearch.cloud.monitor.metric.common.constant.Constants;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config.FlumeConfigMapBuilder;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config.MonitorConfig;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config.OpentsdbConfigMapBuilder;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config.SlsConfigMapBuilder;
import com.elasticsearch.cloud.monitor.metric.common.monitor.opentsdb.OpentsdbSink;
import com.elasticsearch.cloud.monitor.sdk.sink.SlsMonitorSink;
import com.opensearch.cobble.monitor.Monitor;
import com.opensearch.cobble.monitor.MonitorFactory;
import com.taobao.kmonitor.impl.KMonitorConfig;
import com.taobao.kmonitor.sink.flume.FlumeSink;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.elasticsearch.cloud.monitor.metric.common.monitor.opentsdb.OpentsdbSink.CONFIG_REPORT_SWITCH_KEY;
import static com.elasticsearch.cloud.monitor.sdk.sink.SlsMonitorSink.CONFIG_REPORT_SWITCH;
import static com.taobao.kmonitor.sink.flume.FlumeSink.DEFAULT_FLUME_ADDRESS;

/**
 * @author xiaoping
 * @date 2019/11/28
 */
@Slf4j
public class KmonCreator {
    private final static String DEFAULT_GROUP = "default";
    public final static String DEFAULT_TENANT = "default";

    public volatile static boolean inited = false;
    private static Object lock = new Object();

    private static void init(Map<String, String> params) {
        if (!inited) {
            MonitorConfig monitorConfig = constructMonitorConfig(params);
            doInit(monitorConfig);
        }
    }

    public static void doInit(MonitorConfig monitorConfig) {
        if (!inited) {
            synchronized (lock) {
                if (!inited) {
                    addCustomSink(monitorConfig.getSlsConfigMapBuilder(), monitorConfig.getOpentsdbConfigMapBuilder());
                    addFlumeSink(monitorConfig.getFlumeConfigMapBuilder());
                    inited = true;
                }
            }
        }
    }

    public static MonitorConfig constructMonitorConfig(Map<String, String> params) {
        SlsConfigMapBuilder slsConfigMapBuilder = new SlsConfigMapBuilder()
            .project(params.getOrDefault(Constants.MONITOR_SLS_PROJECT, ""))
            .endpoint(params.getOrDefault(Constants.MONITOR_SLS_ENDPOINT, ""))
            .logstore(params.getOrDefault(Constants.MONITOR_SLS_LOGSTORE, ""))
            .accesskey(params.getOrDefault(Constants.MONITOR_SLS_ACCESS_KEY, ""))
            .accesssecret(params.getOrDefault(Constants.MONITOR_SLS_ACCESS_SECRET, ""))
            .reportEnable(Boolean.valueOf(params.getOrDefault(Constants.MONITOR_SLS_REPORT_ENABLE, "false")));

        OpentsdbConfigMapBuilder opentsdbConfigMapBuilder = new OpentsdbConfigMapBuilder()
            .endpoint(params.getOrDefault(Constants.MONITOR_OPENTSDB_ENDPOINT, ""))
            .reportEnable(Boolean.valueOf(params.getOrDefault(Constants.MONITOR_OPENTSDB_REPORT_ENABLE, "false")));

        FlumeConfigMapBuilder flumeConfigMapBuilder = new FlumeConfigMapBuilder()
            .address(params.getOrDefault(Constants.MONITOR_KMON_FLUME_ADDRESS, DEFAULT_FLUME_ADDRESS))
            .enable(Boolean.valueOf(params.getOrDefault(Constants.MONITOR_KMON_FLUME_REPORT_ENABLE, "true")))
            .assigned(Boolean.valueOf(params.getOrDefault(Constants.MONITOR_KMON_FLUME_ADDRESS_ASSIGNED, "false")));
        return new MonitorConfig(slsConfigMapBuilder, flumeConfigMapBuilder, opentsdbConfigMapBuilder);
    }

    public static Monitor getMonitor(Map<String, String> params, Map<String, String> globalTags) {
        try {
            init(params);
            String serviceName = params.getOrDefault(Constants.MONITOR_SEREVICE_NAME,
                Constants.MONITOR_SEREVICE_NAME_DEF);
            String tenant = params.getOrDefault(Constants.MONITOR_TENANT_NAME, DEFAULT_TENANT);

            return getMonitor(serviceName, tenant, globalTags);
        } catch (Throwable t) {
            log.error("getMonitor error", t);
            return null;
        }
    }

    private static void addCustomSink(SlsConfigMapBuilder slsConfigMapBuilder,
        OpentsdbConfigMapBuilder opentsdbConfigMapBuilder) {
        Map<String, String> slsSinkConfMap = slsConfigMapBuilder.build();
        if (Boolean.valueOf(slsSinkConfMap.getOrDefault(CONFIG_REPORT_SWITCH, "false"))) {
            log.error("addCustomSink sls");
            slsSinkConfMap.put("class", SlsMonitorSink.class.getName());
            KMonitorConfig.addSink("sls", slsSinkConfMap);
        }

        Map<String, String> opentsdbSinkConfMap = opentsdbConfigMapBuilder.build();
        if (Boolean.valueOf(opentsdbSinkConfMap.getOrDefault(CONFIG_REPORT_SWITCH_KEY, "false"))) {
            opentsdbSinkConfMap.put("class", OpentsdbSink.class.getName());
            KMonitorConfig.addSink("opentsdb", opentsdbSinkConfMap);
        }

    }

    private static void addFlumeSink(FlumeConfigMapBuilder builder) {
        Map<String, String> sinkConfMap = builder.build();
        boolean enable = Boolean.valueOf(sinkConfMap.getOrDefault(CONFIG_REPORT_SWITCH_KEY, "true"));
        if (enable) {
            sinkConfMap.put("class", FlumeSink.class.getName());
        } else {
            sinkConfMap.put("class", "");
        }

        KMonitorConfig.addSink("flume", sinkConfMap);
    }

    public static Monitor getMonitor(String serviceName, String tenant, Map<String, String> globalTags) {
        KMonitorConfig.setKMonitorServiceName(serviceName);
        KMonitorConfig.setKMonitorTenantName(tenant);
        KMonitorConfig.enableSystemMetrics();
        if (globalTags != null) {
            for (String key : globalTags.keySet()) {
                KMonitorConfig.getGlobalTags().addTag(key, globalTags.get(key));
            }
        }

        Monitor monitor = MonitorFactory.getKMonitor(DEFAULT_GROUP);

        MonitorFactory.start();
        return monitor;
    }
}
