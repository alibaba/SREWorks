package com.elasticsearch.cloud.monitor.metric.log.udtf;

import com.elasticsearch.cloud.monitor.metric.common.blink.utils.BlinkTagsUtil;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.KmonCreatorForBlink;
import com.elasticsearch.cloud.monitor.metric.log.common.Constant;
import com.elasticsearch.cloud.monitor.metric.log.common.Util;
import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.opensearch.cobble.monitor.Monitor;
import com.taobao.kmonitor.core.MetricsTags;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.TableFunction;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author xiaoping
 * @date 2021/3/8
 */
@Deprecated
public class ExtractMetric extends TableFunction<Tuple2<String, String>> {
    private static final Log log = LogFactory.getLog(ExtractMetric.class);
    private transient Monitor monitor;
    private transient MetricsTags tags;
    private final String LOG_TYPE_SEARCH = "search";
    private final String LOG_TYPE_INDEX = "index";

    private final String TOOK_MILLIS = "took_millis";
    private final String TOTAL_HITS = "total_hits";
    private final String SEARCH_TYPE = "search_type";
    private final String TYPE = "type";

    private final String METRIC_PREFIX = "elasticsearch-log";
    private final String SEARCH_COUNT_METRIC = "slow.search.count";
    private final String SEARCH_TIME_METRIC = "slow.search.time";
    private final String SEARCH_TOTAL_HITS_METRIC = "slow.search.total_hits";

    private final String INDEX_COUNT_METRIC = "slow.index.count";
    private final String INDEX_TIME_METRIC = "slow.index.time";

    private final String TOOK_TIME_COMPARE_LIST = "took.time.compare.list";
    private transient TreeMap<Double, String> compareTimes;

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
        }
        tags = BlinkTagsUtil.getTags(context, this.getClass().getSimpleName());
        String timeListString = context.getJobParameter(TOOK_TIME_COMPARE_LIST, "");
        if (StringUtils.isNotEmpty(timeListString)) {
            List<String> timeList = Splitter.on(",").splitToList(timeListString);
            compareTimes = new TreeMap<>();
            for (String time : timeList) {
                compareTimes.put(Util.parseToMillis(time), time);
            }
            log.error(String.format("took.time.compare.list is %s", new Gson().toJson(compareTimes)));
        }

    }

    public void eval(String logContent, String timestampSecond, String esInstanceId, String ip, String level,
        String logType) {
        try {
            doEval(logContent, timestampSecond, esInstanceId, ip, level, logType);
        } catch (Throwable t) {
            log.error(String
                .format("ExtractMetric log error %s of content %s  instanceId %s", t.getMessage(), logContent,
                    esInstanceId), t);
        }

    }

    private void doEval(String logContent, String timestampSecond, String esInstanceId, String ip, String level,
        String logType) throws Exception {
        if (StringUtils.isEmpty(logContent)) {
            return;
        }
        reportMetric(logContent.getBytes().length, timestampSecond);
        if (LOG_TYPE_SEARCH.equals(logType)) {
            doEvalSlowSearchLog(logContent, timestampSecond, esInstanceId, ip, level);
        } else if (LOG_TYPE_INDEX.equals(logType)) {
            doEvalSlowIndexLog(logContent, timestampSecond, esInstanceId, ip, level);
        }
    }

    private void doEvalSlowIndexLog(String logContent, String timestampSecond, String esInstanceId, String ip,
        String level) throws Exception {
        List<String> contents = Util.indexSplitter.splitToList(logContent);
        if (contents == null || contents.size() < 4) {
            log.error(String
                .format("ExtractMetric invalid slow index log of content length, content %s instanceId %s",
                    logContent,
                    esInstanceId));
            return;
        }

        String indexNameAndUUid = contents.get(2);
        String indexName = null;
        if ("]".equals(contents.get(1))) {
            indexNameAndUUid = contents.get(3);
        }
        String[] indexNameAndUUidArray = indexNameAndUUid.split("/");
        if (indexNameAndUUidArray.length == 1) {
            indexName = indexNameAndUUidArray[0].substring(1, indexNameAndUUidArray[0].length() - 1);
        } else if (indexNameAndUUidArray.length == 2) {
            indexName = indexNameAndUUidArray[0].substring(1);
        } else if (indexNameAndUUidArray.length > 2) {
            int suffixLength = indexNameAndUUidArray[indexNameAndUUidArray.length - 1].length() + 1;
            indexName = indexNameAndUUid.substring(1, indexNameAndUUid.length() - suffixLength);
        }
        if (StringUtils.isEmpty(indexName)) {
            log.error(String
                .format("ExtractMetric invalid slow index log of indexNameAndUUid %s, content %s instanceId %s",
                    indexNameAndUUid, logContent, esInstanceId));
            return;
        }

        Double tookMillis = null;
        String docType = null;
        int count = 0;
        for (int i = 3; i < contents.size(); i++) {
            if (count == 2) {
                break;
            }
            String content = contents.get(i);
            if (content.startsWith(TOOK_MILLIS)) {
                tookMillis = getDigitFromString(content, TOOK_MILLIS.length() + 1, content.length() - 1);
                count++;
            } else if (content.startsWith(TYPE)) {
                docType = content.substring(TYPE.length() + 1, content.length() - 2);
                count++;
            }
        }
        if (tookMillis == null) {
            log.error(String
                .format(
                    "ExtractMetric invalid slow index log of took millis %s, content %s "
                        + "instanceId %s", tookMillis, logContent, esInstanceId));
            return;
        }
        String tag;
        if (StringUtils.isNotEmpty(docType)) {
            tag = String.format("instance_id=%s ip=%s index=%s level=%s type=%s", esInstanceId, ip, indexName, level,
                docType);
        } else {
            tag = String.format("instance_id=%s ip=%s index=%s level=%s", esInstanceId, ip, indexName, level);
        }

        String hash = String.valueOf(tag.hashCode());
        String metric = String.format("%s.%s %s000 %s %s", METRIC_PREFIX, INDEX_COUNT_METRIC, timestampSecond, 1,
            tag);
        collectAndReport(metric, hash);

        metric = String.format("%s.%s %s000 %s %s", METRIC_PREFIX, INDEX_TIME_METRIC, timestampSecond, tookMillis, tag);
        collectAndReport(metric, hash);

        reportGreatThanTimeCountMetric(METRIC_PREFIX + "." + INDEX_COUNT_METRIC, tag, tookMillis, timestampSecond,
            hash);

    }

    private void doEvalSlowSearchLog(String logContent, String timestampSecond, String esInstanceId, String ip,
        String level) throws Exception {
        List<String> contents = Util.searchSplitter.splitToList(logContent);
        if (contents == null || contents.size() < 4) {
            log.error(String
                .format("ExtractMetric invalid slow search log of content length, content %s instanceId %s",
                    logContent,
                    esInstanceId));
            return;
        }

        String slowSearchType;
        String indexName;
        String shardId;
        if (contents.get(0).contains(".query")) {
            slowSearchType = "query";
        } else if (contents.get(0).contains(".fetch")) {
            slowSearchType = "fetch";
        } else {
            log.error(String
                .format("ExtractMetric invalid slow search log of slowSearchType, content %s instanceId %s",
                    logContent,
                    esInstanceId));
            return;

        }
        String[] indexAndShard = contents.get(2).split("]\\[");
        if (indexAndShard.length != 2) {
            indexAndShard = contents.get(3).split("]\\[");
        }
        if (indexAndShard.length != 2) {
            log.error(String
                .format("ExtractMetric invalid slow search log of index and shard %s, content %s instanceId %s",
                    new Gson().toJson(indexAndShard), logContent, esInstanceId));
            return;
        }
        indexName = indexAndShard[0].substring(1);
        shardId = indexAndShard[1].substring(0, indexAndShard[1].length() - 1);
        Double tookMillis = null;
        Double totalHits = null;
        String searchType = null;
        int count = 0;
        for (int i = 3; i < contents.size(); i++) {
            if (count == 3) {
                break;
            }
            String content = contents.get(i);
            if (content.startsWith(TOOK_MILLIS)) {
                tookMillis = getDigitFromString(content, TOOK_MILLIS.length() + 1, content.length() - 1);
                count++;
            } else if (content.startsWith(TOTAL_HITS)) {
                totalHits = getDigitFromString(content, TOTAL_HITS.length() + 1, content.length() - 1);
                count++;
            } else if (content.startsWith(SEARCH_TYPE)) {
                searchType = content.substring(SEARCH_TYPE.length() + 1, content.length() - 2);
                count++;
            }
        }
        if (tookMillis == null || StringUtils.isEmpty(searchType)) {
            log.error(String
                .format(
                    "ExtractMetric invalid slow search log of took %s   and search type %s, content %s "
                        + "instanceId %s", tookMillis, searchType, logContent, esInstanceId));
            return;
        }
        String tag = String.format(
            "instance_id=%s ip=%s index=%s shard=%s search_type=%s level=%s slow_log_type=%s", esInstanceId,
            ip, indexName, shardId, searchType, level, slowSearchType);
        String hash = String.valueOf(tag.hashCode());
        String metric = String.format("%s.%s %s000 %s %s", METRIC_PREFIX, SEARCH_COUNT_METRIC, timestampSecond, 1,
            tag);
        collectAndReport(metric, hash);

        metric = String.format("%s.%s %s000 %s %s", METRIC_PREFIX, SEARCH_TIME_METRIC, timestampSecond, tookMillis,
            tag);
        collectAndReport(metric, hash);
        reportGreatThanTimeCountMetric(METRIC_PREFIX + "." + SEARCH_COUNT_METRIC, tag, tookMillis, timestampSecond,
            hash);
        if (totalHits != null) {
            metric = String.format("%s.%s %s000 %s %s", METRIC_PREFIX, SEARCH_TOTAL_HITS_METRIC, timestampSecond,
                totalHits, tag);
            collectAndReport(metric, hash);
        }
    }

    private void reportGreatThanTimeCountMetric(String metricPre, String tag, Double timeTook, String timestampSecond,
        String hash) {
        if (compareTimes != null) {
            for (Entry<Double, String> entry : compareTimes.entrySet()) {
                if (timeTook >= entry.getKey()) {
                    String metric = String.format("%s.gt.%s %s000 %s %s", metricPre, entry.getValue(), timestampSecond,
                        1, tag);
                    collectAndReport(metric, String.format("%s%s", hash, entry.getValue()));
                } else {
                    break;
                }
            }
        }

    }

    private void collectAndReport(String metric, String hash) {
        if (monitor != null) {
            monitor.increment(Constant.LOG_EXTRACT_OUTPUT_TPS, 1, tags);
            monitor.increment(Constant.LOG_EXTRACT_OUTPUT_THROUGHPUT, metric.getBytes().length, tags);
        }
        collect(Tuple2.of(metric, hash));
    }

    private Double getDigitFromString(String content, int start, int end) throws Exception {
        int i = start;
        for (; i <= end; i++) {
            if (content.charAt(i) < '0' || content.charAt(i) > '9') {
                break;
            }
        }
        if (start == i) {
            throw new Exception(String.format("getDigitFromString start %s of %s is not digital", start, content));
        }
        return Double.valueOf(content.substring(start, i));
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

    @Override
    public void close() {

    }
}
