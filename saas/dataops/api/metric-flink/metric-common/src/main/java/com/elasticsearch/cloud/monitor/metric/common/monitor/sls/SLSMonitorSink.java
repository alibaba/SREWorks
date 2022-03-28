package com.elasticsearch.cloud.monitor.metric.common.monitor.sls;

import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.producer.ILogCallback;
import com.aliyun.openservices.log.producer.LogProducer;
import com.aliyun.openservices.log.producer.ProducerConfig;
import com.aliyun.openservices.log.producer.ProjectConfig;
import com.aliyun.openservices.log.response.PutLogsResponse;
import com.google.common.collect.Lists;
import com.opensearch.cobble.json.JsonUtil;
import com.taobao.kmonitor.core.MetricsRecord;
import com.taobao.kmonitor.core.MetricsSink;
import com.taobao.kmonitor.core.MetricsTag;
import com.taobao.kmonitor.core.MetricsValue;
import org.apache.commons.configuration.SubsetConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.util.Collection;
import java.util.List;

import static com.elasticsearch.cloud.monitor.metric.common.constant.Constants.*;

/**
 * Created by lily on 19/7/1.
 */
public class SLSMonitorSink implements MetricsSink {

    public static final String CONFIG_END_POINT = "endpoint";
    public static final String CONFIG_SLS_PROJECT = "project";
    public static final String CONFIG_ACCESS_KEY = "accesskey";
    public static final String CONFIG_ACCESS_SECRET = "accesssecret";
    public static final String CONFIG_LOG_STORE = "logstore";
    public static final String CONFIG_REPORT_SWITCH = "report_switch";
    private static final Log LOG = LogFactory.getLog(SLSMonitorSink.class);
    private LogProducer producer;
    private ProjectConfig projectConfig;
    private String logStore;
    private volatile boolean isStarted = false;

    @Override
    public void putMetrics(MetricsRecord metricsRecord) {
        if (!isStarted) {
            return;
        }
        if (metricsRecord == null || metricsRecord.metrics() == null) {
            return;
        }
        List<LogItem> logItems = Lists.newArrayList();
        for (MetricsValue metric : metricsRecord.metrics()) {
            LogItem logItem = buildLogItem(metric, metricsRecord);
            if (logItem != null) {
                if (LOG.isDebugEnabled()) {
                    try {
                        LOG.debug(JsonUtil.toJson(logItem));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                logItems.add(logItem);
            }
        }

        if (logItems.size() > 0) {
            try {
                producer.send(projectConfig.projectName, logStore, "", "", logItems, new ILogCallback() {

                    @Override
                    public void onCompletion(PutLogsResponse response, LogException e) {
                        if (e != null) {
                            LOG.error("sls monitor call back ", e);
                        }
                    }
                });
            } catch (Throwable t) {
                LOG.error("sls monitor send error", t);
            }

        }
    }

    @Override
    public void flush() {
        if (!isStarted) {
            return;
        }

        if (producer != null) {
            producer.flush();
        }
    }

    @Override
    public void close() {
        flush();
        isStarted = false;

    }

    @Override
    public void init(SubsetConfiguration subsetConfiguration) {
        boolean configSwitch = subsetConfiguration.getBoolean(CONFIG_REPORT_SWITCH, false);
        LOG.info(String.format("%s=%s", CONFIG_REPORT_SWITCH, configSwitch));
        if (!configSwitch) {
            return;
        }

        String project = subsetConfiguration.getString(CONFIG_SLS_PROJECT);
        LOG.info(String.format("%s=%s", CONFIG_SLS_PROJECT, project));

        String endpoint = subsetConfiguration.getString(CONFIG_END_POINT);
        LOG.info(String.format("%s=%s", CONFIG_END_POINT, endpoint));

        String accessKey = subsetConfiguration.getString(CONFIG_ACCESS_KEY);
        LOG.info(String.format("%s=%s", CONFIG_ACCESS_KEY, accessKey));

        String accessSecret = subsetConfiguration.getString(CONFIG_ACCESS_SECRET);
        LOG.info(String.format("%s=%s", CONFIG_ACCESS_SECRET, accessSecret));

        logStore = subsetConfiguration.getString(CONFIG_LOG_STORE);
        LOG.info(String.format("%s=%s", CONFIG_LOG_STORE, logStore));

        projectConfig = new ProjectConfig(project, endpoint, accessKey, accessSecret);
        ProducerConfig producerConfig = new ProducerConfig();
        //一个package从创建到发送的等待时间
        producerConfig.packageTimeoutInMS = 500;
        producer = new LogProducer(producerConfig);
        producer.setProjectConfig(projectConfig);
        LOG.info("sls monitor sink init succeed");
        isStarted = true;
    }

    public LogItem buildLogItem(MetricsValue metric, MetricsRecord metricsRecord) {
        LogItem logItem = new LogItem();
        Collection<MetricsTag> tags = metricsRecord.tags();
        addLogContentIfNotNull(logItem, MONITOR_SEND_METRIC_NAME_FIELD, metric.name());
        addLogContentIfNotNull(logItem, MONITOR_SEND_METRIC_TIMESTAMP_FIELD, metricsRecord.timestamp() + "");
        addLogContentIfNotNull(logItem, MONITOR_SEND_METRIC_VALUE_FIELD, metric.value().toString());
        if (tags != null) {
            for (MetricsTag metricsTag : tags) {
                addLogContentIfNotNull(logItem, metricsTag.name(), metricsTag.value());
            }
        }
        return logItem;
    }

    public static void addLogContentIfNotNull(LogItem logItem, String key, String value) {
        if (key != null && value != null) {
            logItem.PushBack(key, value);
        }
    }

}
