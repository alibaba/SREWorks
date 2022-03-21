package com.elasticsearch.cloud.monitor.metric.common.blink.utils;

import com.elasticsearch.cloud.monitor.metric.common.constant.Constants;
import com.taobao.kmonitor.ImmutableMetricTags;
import com.taobao.kmonitor.core.MetricsTags;
import org.apache.flink.table.functions.FunctionContext;

/**
 * @author xingming.xuxm
 * @Date 2019-12-02
 */
public class BlinkTagsUtil {
    public static MetricsTags getTags(FunctionContext context, String role) {

        final String tenant = context.getJobParameter(Constants.MONITOR_TENANT_NAME, "default");
//        final String worker = String.format("%s_%d", tenant, context.getIndexOfThisSubtask());
//        MetricsTags tags = new ImmutableMetricTags("worker", worker);
        MetricsTags tags = new ImmutableMetricTags("worker", tenant);
        tags.addTag("source", tenant);
        tags.addTag("role", role);
        return tags;
    }
}
