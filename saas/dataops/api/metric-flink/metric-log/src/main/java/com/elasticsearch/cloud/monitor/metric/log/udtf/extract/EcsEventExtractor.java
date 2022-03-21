package com.elasticsearch.cloud.monitor.metric.log.udtf.extract;

import java.util.List;
import java.util.Map;

import com.elasticsearch.cloud.monitor.metric.log.common.Constant;
import com.elasticsearch.cloud.monitor.metric.log.common.Util;
import com.elasticsearch.cloud.monitor.metric.log.udtf.sync.EsInfoSync;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.opensearch.cobble.monitor.Monitor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author xiaoping
 * @date 2021/4/2
 */
public class EcsEventExtractor {

    private static final Log log = LogFactory.getLog(EcsEventExtractor.class);
    private Monitor monitor;
    private EsInfoSync esInfoSync;
    private Splitter splitter = Splitter.on("/").limit(2);
    public static Gson gson = getGson();

    private static Gson getGson() {
        GsonBuilder gb = new GsonBuilder();
        gb.setLongSerializationPolicy(LongSerializationPolicy.STRING);
        Gson gson = gb.create();
        return gson;
    }

    public EcsEventExtractor(Monitor monitor, EsInfoSync esInfoSync) {
        this.monitor = monitor;
        this.esInfoSync = esInfoSync;
    }

    public String extract(String content, String timeMetrics, String instanceName, String level, String name,
        String regionId, String resourceId, String status) throws InterruptedException {
        Map<String, Object> data = Maps.newHashMap();
        try {
            TimeMetric timeMetric = gson.fromJson(timeMetrics, TimeMetric.class);
            if (timeMetric.getEvent_time() != null) {
                data.put(Constant.event_time, timeMetric.getEvent_time());
            }
        } catch (Throwable t) {
            log.error(String.format("ecs event parse timeMetric %s error %s", timeMetrics, t.getMessage()), t);
        }
        String ecsId = null;
        if (StringUtils.isNotEmpty(resourceId)) {
            List<String> items = splitter.splitToList(resourceId);
            if (items.size() == 2) {
                ecsId = items.get(1);
            }
        }
        if (StringUtils.isEmpty(ecsId)) {
            log.error(String.format("ecs event invalid resourceId %s to ecs id", resourceId));
            return null;
        }
        data.put(Constant.name, name);
        data.put(Constant.status, status);
        data.put(Constant.INSTANCE_ID, instanceName);
        data.put(Constant.CONTENT, content);
        data.put(Constant.LEVEL, level);
        if (esInfoSync != null) {
            if (!esInfoSync.addBizTagByEcsId(instanceName, ecsId, data)) {
                return null;
            }
        }

        String dataString =  gson.toJson(data);
        return dataString;
    }

    @Setter
    @Getter
    public static class TimeMetric {
        private Long event_time;
    }
}
