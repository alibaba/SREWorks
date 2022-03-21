package com.elasticsearch.cloud.monitor.metric.common.metric;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Objects;

/**
 * 指标数据对象
 *
 * @author: fangzong.ly
 * @date: 2022/01/17 17:39
 */
@Data
public class SreworksMetricData {
    private String instanceId;

    private Integer metricId;

    private String metricName;

    private String appId;

    private String type;

    private JSONObject labels;

    private Long timestamp;

    private Double value;

    public boolean fieldValidCheck() {
        return Objects.nonNull(instanceId) && Objects.nonNull(metricId) && Objects.nonNull(metricName) && Objects.nonNull(appId)
                && Objects.nonNull(type) && Objects.nonNull(timestamp) && Objects.nonNull(value);
    }
}
