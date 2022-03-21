package com.elasticsearch.cloud.monitor.metric.common.metric;

import org.junit.Before;
import org.junit.Test;

import java.util.List;


/**
 * @author: fangzong.ly
 * @date: 2021/08/30 20:35
 */
public class MetricTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetMetricInstanceAdRule() throws Exception {
        MetricInstancesAdRuleCache cache = new MetricInstancesAdRuleCache();
        cache.setTeamId("0");
        cache.setAppId("0");
        cache.recovery();
        List<MetricInstanceAdRule> results = cache.getMetricInstanceAdRuleList();
        results.forEach(result -> {
            System.out.println(result);
        });
    }
}
