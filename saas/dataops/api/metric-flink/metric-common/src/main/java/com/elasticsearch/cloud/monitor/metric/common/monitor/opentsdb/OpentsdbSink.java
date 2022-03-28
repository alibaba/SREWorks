package com.elasticsearch.cloud.monitor.metric.common.monitor.opentsdb;

import com.elasticsearch.cloud.monitor.metric.common.uti.HttpClientsUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.opensearch.cobble.json.JsonUtil;
import com.taobao.kmonitor.core.MetricsRecord;
import com.taobao.kmonitor.core.MetricsSink;
import com.taobao.kmonitor.core.MetricsTag;
import com.taobao.kmonitor.core.MetricsValue;
import org.apache.commons.configuration.SubsetConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.elasticsearch.cloud.monitor.metric.common.constant.Constants.*;

/**
 * @author xiaoping
 * @date 2019/12/20
 */
public class OpentsdbSink implements MetricsSink {

    private static final Log log = LogFactory.getLog(OpentsdbSink.class);
    public static final String CONFIG_ENDPOINT_KEY = "endpoint";
    public static final String CONFIG_REPORT_SWITCH_KEY = "report_switch";
    private final int maxTotal = 200;
    private final int maxPerRoute = 40;
    private final int maxRoute = 100;
    private CloseableHttpClient httpClient;
    private volatile boolean isStarted = false;
    private final String postMetricUrl = "/opentsdb";
    private String endpoint;

    @Override
    public void init(SubsetConfiguration configuration) {
        boolean configSwitch = configuration.getBoolean(CONFIG_REPORT_SWITCH_KEY, false);
        log.info(String.format("%s=%s", CONFIG_REPORT_SWITCH_KEY, configSwitch));
        if (!configSwitch) {
            return;
        }

        endpoint = configuration.getString(CONFIG_ENDPOINT_KEY);
        log.info(String.format("%s=%s", CONFIG_ENDPOINT_KEY, endpoint));

        httpClient = HttpClientsUtil.createHttpClient(maxTotal, maxPerRoute, maxRoute, endpoint);
        log.info("opentsdb monitor sink init succeed");
        isStarted = true;
    }

    @Override
    public void putMetrics(MetricsRecord metricsRecord) {
        if (!isStarted) {
            return;
        }
        if (metricsRecord == null || metricsRecord.metrics() == null) {
            return;
        }
        List<Map<String, String>> metricDatas = Lists.newArrayList();
        for (MetricsValue metric : metricsRecord.metrics()) {
            Map<String, String> data = buildMetricData(metric, metricsRecord);
            metricDatas.add(data);
        }
        String response = "{}";
        try {
            response = HttpClientsUtil.post(httpClient, String.format("%s%s", endpoint, postMetricUrl),
                JsonUtil.toJson(metricDatas));
        } catch (Throwable e) {
            log.error("opentsdb sink post metric data error", e);
        }
        try {
            Map<String, Object> resMap = JsonUtil.fromJson(response, new TypeToken<Map<String, Object>>() {}.getType());
            if (!Boolean.valueOf(String.valueOf(resMap.get("success")))) {
                log.error(String.format("  opentsdb api response with error %s", response));
            }
        } catch (JSONException e) {
            log.error("response is not json", e);
        }

    }

    public Map<String, String> buildMetricData(MetricsValue metric, MetricsRecord metricsRecord) {
        Map<String, String> data = Maps.newHashMap();
        Collection<MetricsTag> tags = metricsRecord.tags();
        data.put(MONITOR_SEND_METRIC_NAME_FIELD, metric.name());
        data.put(MONITOR_SEND_METRIC_TIMESTAMP_FIELD, metricsRecord.timestamp() + "");
        data.put(MONITOR_SEND_METRIC_VALUE_FIELD, metric.value().toString());
        if (tags != null) {
            for (MetricsTag metricsTag : tags) {
                data.put(metricsTag.name(), metricsTag.value());
            }
        }
        return data;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("close opentsdb sink http client error", e);
            }
        }

    }
}
