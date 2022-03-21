package com.elasticsearch.cloud.monitor.metric.alarm.blink.utils.cache;

import com.elasticsearch.cloud.monitor.commons.client.SlsConfig;
import com.elasticsearch.cloud.monitor.commons.client.SlsSearchClient;
import com.elasticsearch.cloud.monitor.commons.datapoint.DataPoint;
import com.elasticsearch.cloud.monitor.commons.rule.Rule;
import com.elasticsearch.cloud.monitor.commons.utils.TimeUtils;
import com.elasticsearch.cloud.monitor.metric.alarm.blink.constant.MetricConstants;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class RuleConditionSlsCache extends RuleConditionCache {

    private SlsSearchClient slsSearchClient = null;

    public RuleConditionSlsCache(Rule rule, long interval, SlsConfig slsConfig) {
        super(rule, interval);
        if (slsConfig != null) {
            this.slsSearchClient = new SlsSearchClient(slsConfig);
        }
    }

    /**
     * 不能用eventTimeMs作为截止时间, 因为可能发送延迟
     *
     * @param dataPoint
     * @throws Exception
     */
    @SuppressWarnings("Duplicates")
    @Override
    public void recovery(DataPoint dataPoint)throws IOException {
        //already cache, just return
        if (stateMap.get(dataPoint.getTags()) != null) {
            return;
        }
        long eventTimeMs = TimeUtils.toMillisecond(dataPoint.getTimestamp());
        Preconditions.checkArgument(eventTimeMs % interval == 0,
                "currentEventTimeMs(" + eventTimeMs + ") must be a multiple of " + interval);
        long queryStartMs = eventTimeMs - rule.getDurationCondition().getCrossSpan() + interval;
        if (queryStartMs < eventTimeMs) {
            long start = System.currentTimeMillis();
            List<DataPoint> dps = Lists.newArrayList();
            String query = null;
            try {
                query = getQuery(rule, dataPoint, queryStartMs, eventTimeMs);
                slsSearchClient.search(query, (int) (queryStartMs / 1000), (int) (start / 1000), dps);
                for (DataPoint d : dps) {
                    put(d);
                }
            } catch (Exception e) {
                log.error(String.format("query: %s, exception: %s", query, Throwables.getStackTraceAsString(e)));
                if (monitor != null) {
                    monitor.increment(MetricConstants.ALARM_CACHE_FETECH_ERROR_QPS, 1, globalTags);
                }
            }

            if (monitor != null) {
                monitor.reportLatency(MetricConstants.ALARM_CACHE_QUERY_LATENCY, start, globalTags);
                monitor.increment(MetricConstants.ALARM_CACHE_QUERY_QPS, 1, globalTags);
            }
        }
    }

    private String getQuery(Rule rule, DataPoint dp, long queryStartMs, long currentEventTimeMs) {
        StringBuilder sb = new StringBuilder();
        sb.append("rule_id").append(":").append(rule.getId());
        Map<String, String> tags = dp.getTags();
        for (String k : tags.keySet()) {
            sb.append(" and tags: ").append("\"").append(k).append("=").append(tags.get(k)).append("\"");
        }
        if (dp.getGranularity() != null) {
            sb.append(" and granularity: ").append(dp.getGranularity());
        }
        sb.append(" and timestamp >=").append(queryStartMs);
        sb.append(" and timestamp <=").append(currentEventTimeMs);
        return sb.toString();
    }
}
