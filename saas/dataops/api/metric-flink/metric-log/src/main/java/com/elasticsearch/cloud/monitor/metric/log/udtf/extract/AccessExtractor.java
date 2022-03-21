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
 * @date 2021/3/31
 */
public class AccessExtractor {
    private static final Log log = LogFactory.getLog(AccessExtractor.class);
    private Monitor monitor;
    private EsInfoSync esInfoSync;

    public AccessExtractor(Monitor monitor,
        EsInfoSync esInfoSync) {
        this.monitor = monitor;
        this.esInfoSync = esInfoSync;
    }

    public String extract(String body, String timestampSecond, String esInstanceId, String ip, String bodySize,
        String level, String node, String remote, String uri) throws InterruptedException {
        Map<String, Object> data = Maps.newHashMap();
        data.put(Constant.HOST, ip);
        data.put(Constant.INSTANCE_ID, esInstanceId);

        data.put(Constant.SOURCE, body);
        data.put(Constant.bodySize, Integer.valueOf(bodySize));
        data.put(Constant.LEVEL, level);
        data.put(Constant.node, node);
        data.put(Constant.remote, remote);
        data.put(Constant.uri, uri);
        if (esInfoSync != null) {
            if (!esInfoSync.addBizTag(esInstanceId, ip, data)) {
                return null;
            }
        }

        String dataString = Util.gson.toJson(data);
        return dataString;
    }
}
