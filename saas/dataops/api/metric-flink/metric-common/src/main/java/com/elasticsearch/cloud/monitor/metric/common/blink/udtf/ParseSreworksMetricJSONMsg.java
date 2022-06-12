package com.elasticsearch.cloud.monitor.metric.common.blink.udtf;

import com.alibaba.fastjson.JSONObject;
import com.elasticsearch.cloud.monitor.metric.common.metric.SreworksMetricData;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple8;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.TableFunction;
import org.springframework.util.CollectionUtils;


/**
 * Sreworks指标数据解析
 *
 * 解析成应用ID、指标ID、指标名称、指标类型、实例身份ID、实例tags、指标采集时间、指标数值
 * 入参：sreworks pmdb metric msg
 *
 * @author: fangzong.lyj
 * @date: 2021/09/03 14:54
 */
@Slf4j
public class ParseSreworksMetricJSONMsg extends TableFunction<Tuple8<String, Integer, String, String, String, String, Long, Double>> {

    @Override
    public void open(FunctionContext context) {
    }

    /**
     *
     * @param jsonMsg
     */
    public void eval(String jsonMsg) throws Exception {
        JSONObject msgObject = JSONObject.parseObject(jsonMsg);
        if (CollectionUtils.isEmpty(msgObject)) {
            return;
        }
        SreworksMetricData metricData = JSONObject.toJavaObject(msgObject, SreworksMetricData.class);
        if (metricData.fieldValidCheck()) {
            Tuple8<String, Integer, String, String, String, String, Long, Double> tuple6 = Tuple8.of(metricData.getAppId(),
                    metricData.getMetricId(), metricData.getMetricName(), metricData.getType(), metricData.getInstanceId(),
                    metricData.getLabels().toJSONString(), metricData.getTimestamp(), metricData.getValue());

            collect(tuple6);
        }
    }
}
