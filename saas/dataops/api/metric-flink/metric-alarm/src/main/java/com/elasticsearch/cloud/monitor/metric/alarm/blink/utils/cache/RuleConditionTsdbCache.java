package com.elasticsearch.cloud.monitor.metric.alarm.blink.utils.cache;

import com.elasticsearch.cloud.monitor.commons.core.PlottQueryClient;
import com.elasticsearch.cloud.monitor.commons.datapoint.DataPoint;
import com.elasticsearch.cloud.monitor.commons.rule.Rule;
import com.elasticsearch.cloud.monitor.commons.rule.expression.SelectedMetric;
import com.elasticsearch.cloud.monitor.commons.rule.filter.TagVFilter;
import com.elasticsearch.cloud.monitor.commons.utils.TimeUtils;
import com.elasticsearch.cloud.monitor.metric.alarm.blink.constant.MetricConstants;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.*;

/**
 * 规则缓存(Tsdb源)
 *
 * @author: fangzong.ly
 * @date: 2021/08/31 19:58
 */
public class RuleConditionTsdbCache extends RuleConditionCache{

    private String tsdbAddr;

    public RuleConditionTsdbCache(Rule rule, long interval, String tsdbAddr) {
        super(rule, interval);
        this.tsdbAddr = tsdbAddr;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void recovery(DataPoint dataPoint) throws IOException {
        //already cache, just return
        if (stateMap.get(dataPoint.getTags()) != null) {
            return;
        }

        long currentEventTimeMs = TimeUtils.toMillisecond(dataPoint.getTimestamp());
        Preconditions.checkArgument(currentEventTimeMs % interval == 0, "currentEventTimeMs(" + currentEventTimeMs + ") must be a multiple of " + interval);
        long queryStartMs = currentEventTimeMs - rule.getDurationCondition().getCrossSpan() + interval;
        if (queryStartMs < currentEventTimeMs) {
            try {
                queryAndCache(queryStartMs, currentEventTimeMs, rule, dataPoint);
            } catch (Exception e) {
                throw e;
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void queryAndCache(final long queryStartMs, final long queryEndMs, final Rule rule, DataPoint dataPoint) throws IOException {
        long start = System.currentTimeMillis();
        if (queryClient == null) {
            queryClient = new PlottQueryClient(tsdbAddr, "application/x-www-form-urlencoded");
        }

        Map<String, TagVFilter> filters = new HashMap<>();
        List<TagVFilter> tagsFilterList = new LinkedList<>();
        TagVFilter.tagsToFilters(dataPoint.getTags(), tagsFilterList);
        for (TagVFilter tagVFilter : tagsFilterList) {
            filters.put(tagVFilter.getTagk(), tagVFilter);
        }

        if (!rule.isCompose()) {
            List<DataPoint> dps = queryClient.query(queryStartMs, queryEndMs, dataPoint.getName(), filters, interval,
                    rule.getDsAggregator(), rule.getAggregator(), rule.isRate());

            for (DataPoint d : dps) {
                put(d);
            }

        } else {
            Map<String, List<DataPoint>> dmap = new HashMap<>();
            for (SelectedMetric selectedMetric : rule.getMetricCompose().getMetrics()) {
                List<DataPoint> dps;
                if (rule.getMetricCompose().isCrossJoin()) {
                    dps = queryClient.query(queryStartMs, queryEndMs, dataPoint.getName(), selectedMetric.getFilterMap(), interval,
                            selectedMetric.getDsAggregator(), selectedMetric.getAggregator(), selectedMetric.isRate());
                } else {
                    dps = queryClient.query(queryStartMs, queryEndMs, dataPoint.getName(), filters, interval,
                            selectedMetric.getDsAggregator(), selectedMetric.getAggregator(), rule.isRate());
                }
                dmap.put(selectedMetric.getId(), dps);
            }
            mergePut(dmap);
        }

        if (monitor != null) {
            monitor.reportLatency(MetricConstants.ALARM_CACHE_QUERY_LATENCY, start, globalTags);
            monitor.increment(MetricConstants.ALARM_CACHE_QUERY_QPS, 1, globalTags);
        }
    }

    private void mergePut(Map<String, List<DataPoint>> dmap) {
        int size = rule.getMetricCompose().getMetrics().size();
        Map<Long, List<DataPoint>> mergeMap = new HashMap<>();
        for (List<DataPoint> dps : dmap.values()) {
            for (DataPoint dataPoint : dps) {
                List<DataPoint> list = mergeMap.get(dataPoint.getTimestamp());
                if (list == null) {
                    list = new ArrayList<>(size);
                }
                list.add(dataPoint);
                mergeMap.put(dataPoint.getTimestamp(), list);
            }
        }
        for (List<DataPoint> dps : mergeMap.values()) {
            DataPoint compose = rule.evaluate(dps);
            put(compose);
        }
    }
}
