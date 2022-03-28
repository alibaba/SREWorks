package com.elasticsearch.cloud.monitor.metric.common.monitor.kmon;

import com.elasticsearch.cloud.monitor.metric.common.constant.Constants;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config.FlumeConfigMapBuilder;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config.MonitorConfig;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config.OpentsdbConfigMapBuilder;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.config.SlsConfigMapBuilder;
import com.google.common.collect.Maps;
import com.opensearch.cobble.monitor.Monitor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.table.functions.FunctionContext;

import java.util.Map;

import static com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.KmonCreator.DEFAULT_TENANT;
import static com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.KmonCreator.doInit;
import static com.taobao.kmonitor.sink.flume.FlumeSink.DEFAULT_FLUME_ADDRESS;

/**
 * @author xiaoping
 * @date 2019/12/23
 */
@Slf4j
public class KmonCreatorForBlink {
    private static void init(FunctionContext context) {
        if (!KmonCreator.inited) {
            MonitorConfig monitorConfig = constructMonitorConfig(context);
            doInit(monitorConfig);
        }
    }

    public static MonitorConfig constructMonitorConfig(FunctionContext context) {
        SlsConfigMapBuilder slsConfigMapBuilder = new SlsConfigMapBuilder()
            .project(context.getJobParameter(Constants.MONITOR_SLS_PROJECT, ""))
            .endpoint(context.getJobParameter(Constants.MONITOR_SLS_ENDPOINT, ""))
            .logstore(context.getJobParameter(Constants.MONITOR_SLS_LOGSTORE, ""))
            .accesskey(context.getJobParameter(Constants.MONITOR_SLS_ACCESS_KEY, ""))
            .accesssecret(context.getJobParameter(Constants.MONITOR_SLS_ACCESS_SECRET, ""))
            .reportEnable(Boolean.valueOf(context.getJobParameter(Constants.MONITOR_SLS_REPORT_ENABLE, "false")));

        OpentsdbConfigMapBuilder opentsdbConfigMapBuilder = new OpentsdbConfigMapBuilder()
            .endpoint(context.getJobParameter(Constants.MONITOR_OPENTSDB_ENDPOINT, ""))
            .reportEnable(Boolean.valueOf(context.getJobParameter(Constants.MONITOR_OPENTSDB_REPORT_ENABLE, "false")));

        FlumeConfigMapBuilder flumeConfigMapBuilder = new FlumeConfigMapBuilder()
            .address(context.getJobParameter(Constants.MONITOR_KMON_FLUME_ADDRESS, DEFAULT_FLUME_ADDRESS))
            .enable(Boolean.valueOf(context.getJobParameter(Constants.MONITOR_KMON_FLUME_REPORT_ENABLE, "true")))
            .assigned(Boolean.valueOf(context.getJobParameter(Constants.MONITOR_KMON_FLUME_ADDRESS_ASSIGNED, "false")));
        return new MonitorConfig(slsConfigMapBuilder, flumeConfigMapBuilder, opentsdbConfigMapBuilder);
    }

    public static Monitor getMonitor(FunctionContext context, String clazz) {
        try {
            init(context);
            Map<String, String> tags=Maps.newHashMap();
            tags.put("class", clazz);
            return getMonitor(context, tags);
        } catch (Throwable t) {
            log.error("getMonitor error", t);
            return null;
        }
    }

    public static Monitor getMonitor(FunctionContext context, Map<String, String> tags) {
        try {
            init(context);
            String serviceName = context.getJobParameter(Constants.MONITOR_SEREVICE_NAME,
                Constants.MONITOR_SEREVICE_NAME_DEF);
            String tenant = context.getJobParameter(Constants.MONITOR_TENANT_NAME, DEFAULT_TENANT);
            String job = context.getJobParameter(Constants.MONITOR_JOB_NAME, DEFAULT_TENANT);
            if (tags == null) {
                tags = Maps.newHashMap();
            }
            tags.put("job", job);
//            tags.put("taskId", String.valueOf(context.getIndexOfThisSubtask()));
            return KmonCreator.getMonitor(serviceName, tenant, tags);
        } catch (Throwable t) {
            log.error("getMonitor error", t);
            return null;
        }
    }

}
