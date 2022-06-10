package com.elasticsearch.cloud.monitor.metric.common.blink.udf;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flink.table.functions.ScalarFunction;
import org.springframework.util.DigestUtils;

import java.sql.Timestamp;
import java.util.Map;

/**
 * SREWorks PMDB指标唯一身份ID
 *
 * 入参：metricName labels
 *
 * @author: fangzong.lyj
 * @date: 2022/06/09 15:17
 */
public class PmdbMetricUidUdf extends ScalarFunction {
    private static final Log log = LogFactory.getLog(PmdbMetricUidUdf.class);

    public String eval(String metricName, Map<String, String> labels) {
        return DigestUtils.md5DigestAsHex((metricName + "|" + JSONObject.toJSONString(labels)).getBytes());
    }
}
