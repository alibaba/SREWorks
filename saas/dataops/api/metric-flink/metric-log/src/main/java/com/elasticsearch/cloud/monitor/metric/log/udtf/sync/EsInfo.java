package com.elasticsearch.cloud.monitor.metric.log.udtf.sync;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xiaoping
 * @date 2021/3/30
 */
@Setter
@Getter
public class EsInfo {
    private String regionId;
    private String uid;
    private String instanceId;
    private String resourceGroup;
    private Map<String, String> ipToZone;
    /**
     * ip -> nodeInfoMap
     */
    private Map<String, EcsInfo> nodeInfoMap;

    @Setter
    @Getter
    public static class EcsInfo {
        private String ecsId;
        private String ecsIp;
    }
}
