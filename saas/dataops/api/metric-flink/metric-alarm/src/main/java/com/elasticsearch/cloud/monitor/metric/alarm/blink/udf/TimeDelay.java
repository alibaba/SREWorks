package com.elasticsearch.cloud.monitor.metric.alarm.blink.udf;

import com.elasticsearch.cloud.monitor.metric.alarm.blink.constant.MetricConstants;
import com.elasticsearch.cloud.monitor.metric.common.blink.utils.BlinkTagsUtil;
import com.elasticsearch.cloud.monitor.metric.common.monitor.kmon.KmonCreatorForBlink;
import com.opensearch.cobble.monitor.Monitor;
import com.taobao.kmonitor.core.MetricsTags;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

/**
 * 为什么没有写到SQL里面, 就是为了能汇报一下 metric
 * 也没啥大用~
 *
 * @author xingming.xuxm
 * @Date 2019-12-04
 */
public class TimeDelay extends ScalarFunction {
    private Monitor monitor;
    private MetricsTags tags;
    private long maxDelayThreshold = 15 * 60 * 1000L;

    @Override
    public void open(FunctionContext context) {
        monitor = KmonCreatorForBlink.getMonitor(context, this.getClass().getSimpleName());
        monitor.registerGauge(MetricConstants.ALARM_DATA_DELAY);
        monitor.registerQPS(MetricConstants.ALARM_ERROR_QPS);
        tags = BlinkTagsUtil.getTags(context, this.getClass().getSimpleName());

        String delay = context.getJobParameter("data.max_delay", "");
        if (!delay.isEmpty()) {
            maxDelayThreshold = Long.parseLong(delay);
        }
    }

    public boolean eval(long timestamp) {
        return eval(timestamp, maxDelayThreshold);
    }

    public boolean eval(long timestamp, long maxDelay) {
        long delay = System.currentTimeMillis() - timestamp;
        if (delay > maxDelay) {
            if (monitor != null) {
                monitor.reportLatency(MetricConstants.ALARM_DATA_DELAY, timestamp, tags);
            }
            return true;
        }

        if (delay < 0) {
            if (monitor != null) {
                monitor.increment(MetricConstants.ALARM_ERROR_QPS, 1, tags);
            }
            return true;
        }
        return false;
    }
}
