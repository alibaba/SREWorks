package com.elasticsearch.cloud.monitor.metric.common.blink.udf;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flink.table.functions.ScalarFunction;
import org.springframework.util.DigestUtils;

import java.util.Map;

/**
 * SREWorks PMDB指标实例唯一身份ID
 *
 * 入参：metricId labels
 *
 * @author: fangzong.lyj
 * @date: 2022/06/09 15:17
 */
public class PmdbMetricInsUidUdf extends ScalarFunction {
    private static final Log log = LogFactory.getLog(PmdbMetricInsUidUdf.class);

    public String eval(Integer metricId, Map<String, String> labels) {
        return DigestUtils.md5DigestAsHex((metricId + "|" + JSONObject.toJSONString(labels)).getBytes());
    }
}
