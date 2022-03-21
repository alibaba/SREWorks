package com.elasticsearch.cloud.monitor.metric.alarm.blink.utils.cache;

import com.elasticsearch.cloud.monitor.commons.datapoint.DataPoint;
import com.elasticsearch.cloud.monitor.commons.rule.Rule;
import com.elasticsearch.cloud.monitor.metric.common.client.KafkaConfig;

import java.io.IOException;

/**
 * 规则缓存(Kafka源)
 *
 * @author: fangzong.ly
 * @date: 2021/08/31 19:58
 */
public class RuleConditionKafkaCache extends RuleConditionCache{

    public RuleConditionKafkaCache(Rule rule, long interval, KafkaConfig kafkaConfig) {
        super(rule, interval);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void recovery(DataPoint dataPoint) throws IOException {

    }
}
