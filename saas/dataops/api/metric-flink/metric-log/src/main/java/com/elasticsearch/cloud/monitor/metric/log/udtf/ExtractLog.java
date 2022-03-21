package com.elasticsearch.cloud.monitor.metric.log.udtf;

import java.util.Set;

import com.elasticsearch.cloud.monitor.metric.common.blink.utils.BlinkTagsUtil;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.KmonCreatorForBlink;
import com.elasticsearch.cloud.monitor.metric.log.common.Constant;
import com.elasticsearch.cloud.monitor.metric.log.udtf.extract.AccessExtractor;
import com.elasticsearch.cloud.monitor.metric.log.udtf.extract.EcsEventExtractor;
import com.elasticsearch.cloud.monitor.metric.log.udtf.extract.InstanceExtractor;
import com.elasticsearch.cloud.monitor.metric.log.udtf.extract.SlowIndexExtractor;
import com.elasticsearch.cloud.monitor.metric.log.udtf.extract.SlowSearchExtractor;
import com.elasticsearch.cloud.monitor.metric.log.udtf.sync.EsInfoSync;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.opensearch.cobble.monitor.Monitor;
import com.taobao.kmonitor.MetricType;
import com.taobao.kmonitor.StatisticsType;
import com.taobao.kmonitor.core.MetricsTags;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.TableFunction;

/**
 * @author xiaoping
 * @date 2021/3/8
 */
public class ExtractLog
    extends TableFunction<Tuple1<String>> {
    private static final Log log = LogFactory.getLog(ExtractLog.class);
    private transient Monitor monitor;
    private transient MetricsTags tags;
    private final String LOG_TYPE_SEARCH = "search";
    private final String LOG_TYPE_INDEX = "index";

    private transient EsInfoSync esInfoSync;

    private transient SlowIndexExtractor slowIndexExtractor;
    private transient SlowSearchExtractor slowSearchExtractor;
    private transient AccessExtractor accessExtractor;
    private transient InstanceExtractor instanceExtractor;
    private transient EcsEventExtractor ecsEventExtractor;

    private String region;
    private Set<String> blackInstances = null;

    @Override
    public void open(FunctionContext context) {
        monitor = KmonCreatorForBlink.getMonitor(context, this.getClass().getSimpleName());
        if (monitor != null) {
            monitor.registerGauge(Constant.LOG_EXTRACT_SINGLE_DOC_SIZE);
            monitor.registerGauge(Constant.LOG_EXTRACT_LATENCY);
            monitor.registerQPS(Constant.LOG_EXTRACT_TPS);
            monitor.registerQPS(Constant.LOG_EXTRACT_THROUGHPUT);

            monitor.registerQPS(Constant.LOG_EXTRACT_OUTPUT_TPS);
            monitor.registerQPS(Constant.LOG_EXTRACT_OUTPUT_THROUGHPUT);
            monitor.register(Constant.LOG_UNKNOWN_INSTANCE_COUNT, MetricType.GAUGE, StatisticsType.SUM);
        }
        tags = BlinkTagsUtil.getTags(context, this.getClass().getSimpleName());
        esInfoSync = new EsInfoSync(context, monitor);
        slowIndexExtractor = new SlowIndexExtractor(monitor, esInfoSync);
        slowSearchExtractor = new SlowSearchExtractor(monitor, esInfoSync);
        accessExtractor = new AccessExtractor(monitor, esInfoSync);
        instanceExtractor = new InstanceExtractor(monitor, esInfoSync);
        ecsEventExtractor = new EcsEventExtractor(monitor, esInfoSync);
        region = context.getJobParameter("current_region", "");
        String blackInstancesParam = context.getJobParameter("black_instance", "");
        if (StringUtils.isNotEmpty(blackInstancesParam)) {
            blackInstances = Sets.newConcurrentHashSet(Splitter.on(",").splitToList(blackInstancesParam));
        }
    }

    /**
     * slow search and index
     */
    public void eval(String logContent, String timestampSecond, String esInstanceId, String ip, String level,
        String logType) {
        try {
            if (isBlackInstance(esInstanceId)) { return; }
            doEval(logContent, timestampSecond, esInstanceId, ip, level, logType);
        } catch (Throwable t) {
            log.error(String
                .format("ExtractMetric log error %s of content %s  instanceId %s", t.getMessage(), logContent,
                    esInstanceId), t);
        }
    }

    /**
     * for search access
     */
    public void eval(String body, String timestampSecond, String esInstanceId, String ip, String bodySize, String level,
        String node, String remote, String uri) {
        try {
            if (isBlackInstance(esInstanceId)) { return; }
            reportMetric(body.getBytes().length + uri.getBytes().length, timestampSecond);
            String dataJson = accessExtractor.extract(body, timestampSecond, esInstanceId, ip, bodySize, level,
                node, remote, uri);
            if (StringUtils.isNotEmpty(dataJson)) {
                collect(Tuple1.of(dataJson));
            }
        } catch (Throwable t) {
            log.error(String
                .format("ExtractMetric log error search access of body %s  instanceId %s of ip %s of time %s, error %s",
                    body, esInstanceId, ip, timestampSecond, t.getMessage()), t);
        }
    }

    /**
     * for instance log
     */
    public void eval(String content, String timestampSecond, String esInstanceId, String ip, String level) {
        try {
            if (isBlackInstance(esInstanceId)) { return; }
            reportMetric(content.getBytes().length, timestampSecond);
            String dataJson = instanceExtractor.extract(content, timestampSecond, esInstanceId, ip, level);
            if (StringUtils.isNotEmpty(dataJson)) {
                collect(Tuple1.of(dataJson));
            }
        } catch (Throwable t) {
            log.error(String
                .format(
                    "ExtractMetric log error main instance of content %s  instanceId %s of ip %s of time %s, error %s",
                    content, esInstanceId, ip, timestampSecond, t.getMessage()), t);
        }
    }

    /**
     * for ecs event log
     */
    public void eval(String content, String timeMetrics, String instanceName, String level, String name,
        String regionId, String resourceId, String status, String timestampSecond, String extra) {
        try {
            if (isBlackInstance(instanceName)) { return; }
            if (StringUtils.isEmpty(region) || !region.equals(regionId)) {
                return;
            }
            reportMetric(content.getBytes().length, timestampSecond);
            String dataJson = ecsEventExtractor.extract(content, timeMetrics, instanceName, level, name,
                regionId, resourceId, status);
            if (StringUtils.isNotEmpty(dataJson)) {
                collect(Tuple1.of(dataJson));
            }
        } catch (Throwable t) {
            log.error(String
                .format(
                    "ExtractMetric log error main instance of content %s  instanceId %s of resourceId %s of time %s, "
                        + "error %s",
                    content, instanceName, resourceId, timestampSecond, t.getMessage()), t);
        }
    }

    private void doEval(String logContent, String timestampSecond, String esInstanceId, String ip, String level,
        String logType) throws Exception {
        if (StringUtils.isEmpty(logContent)) {
            return;
        }
        reportMetric(logContent.getBytes().length, timestampSecond);
        String dataJsonString = null;
        if (LOG_TYPE_SEARCH.equals(logType)) {
            dataJsonString = slowSearchExtractor.extract(logContent, timestampSecond, esInstanceId, ip, level);
        } else if (LOG_TYPE_INDEX.equals(logType)) {
            dataJsonString = slowIndexExtractor.extract(logContent, timestampSecond, esInstanceId, ip, level);
        }
        if (StringUtils.isNotEmpty(dataJsonString)) {
            collect(Tuple1.of(dataJsonString));
        }
    }

    private void reportMetric(long size, String timestampSecond) {
        if (monitor != null) {
            monitor.reportLatency(Constant.LOG_EXTRACT_LATENCY,
                Integer.valueOf(timestampSecond) * 1000L, tags);
            monitor.increment(Constant.LOG_EXTRACT_TPS, 1, tags);
            monitor.increment(Constant.LOG_EXTRACT_SINGLE_DOC_SIZE, size, tags);
            monitor.increment(Constant.LOG_EXTRACT_THROUGHPUT, size, tags);
        }
    }

    private boolean isBlackInstance(String instanceId) {
        if (blackInstances == null || !blackInstances.contains(instanceId)) {
            return false;
        }
        return true;
    }

    @Override
    public void close() {
        if (esInfoSync != null) {
            esInfoSync.close();
        }

    }
}
