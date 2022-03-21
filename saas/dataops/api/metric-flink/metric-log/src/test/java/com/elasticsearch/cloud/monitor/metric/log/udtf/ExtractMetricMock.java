package com.elasticsearch.cloud.monitor.metric.log.udtf;

import java.util.Set;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * @author xiaoping
 * @date 2021/3/11
 */
@Setter
@Getter
@NoArgsConstructor
public class ExtractMetricMock extends ExtractMetric {
    private Set<String> metrics = Sets.newHashSet();

    @Override
    public void collect(Tuple2<String, String> row) {
        metrics.add(row.f0);
    }
}
