package com.elasticsearch.cloud.monitor.metric.log.udtf.extract;

import java.util.Map;

import com.elasticsearch.cloud.monitor.metric.log.common.Constant;
import com.elasticsearch.cloud.monitor.metric.log.common.Util;
import com.elasticsearch.cloud.monitor.metric.log.udtf.sync.EsInfoSync;
import com.google.common.collect.Maps;
import com.opensearch.cobble.monitor.Monitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author xiaoping
 * @date 2021/4/2
 */
public class InstanceExtractor {

    private static final Log log = LogFactory.getLog(InstanceExtractor.class);
    private Monitor monitor;
    private EsInfoSync esInfoSync;

    public InstanceExtractor(Monitor monitor, EsInfoSync esInfoSync) {
        this.monitor = monitor;
        this.esInfoSync = esInfoSync;
    }

    public String extract(String content, String timestampSecond, String esInstanceId, String ip, String level)
        throws InterruptedException {
        Map<String, Object> data = Maps.newHashMap();
        data.put(Constant.HOST, ip);
        data.put(Constant.INSTANCE_ID, esInstanceId);

        data.put(Constant.CONTENT, content);
        data.put(Constant.LEVEL, level);
        if (esInfoSync != null) {
            if (!esInfoSync.addBizTag(esInstanceId, ip, data)) {
                return null;
            }
        }

        String dataString = Util.gson.toJson(data);
        return dataString;
    }
}
