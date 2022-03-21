package com.elasticsearch.cloud.monitor.metric.log.common;

/**
 * @author xiaoping
 * @date 2021/3/8
 */
public class Constant {
    public static final String LOG_EXTRACT_TPS = "log.extract.tps";
    public static final String LOG_EXTRACT_THROUGHPUT = "log.extract.throughput";
    public static final String LOG_EXTRACT_SINGLE_DOC_SIZE = "log.extract.single.doc_size";
    public static final String LOG_EXTRACT_LATENCY = "log.extract.latency";

    public static final String LOG_EXTRACT_OUTPUT_TPS = "log.extract.output.tps";
    public static final String LOG_EXTRACT_OUTPUT_THROUGHPUT = "log.extract.output.throughput";

    public static final String LOG_UNKNOWN_INSTANCE_COUNT = "log.unknown.instance.count";

    public static final String INDEX_NAME = "index_name";
    public static final String SHARD_ID = "shard_id";
    public static final String SEARCH_TYPE = "search_type";
    public static final String SLOW_SEARCH_LOG_TYPE = "slow_search_log_type";
    /**
     * 记得最后统一性key名称
     */
    public static final String SOURCE = "source";
    public static final String CONTENT = "content";
    public static final String SEARCH_TIME_MS = "search_time_ms";
    public static final String INDEX_TIME_MS = "index_time_ms";
    public static final String SEARCH_TOTAL_HITS = "search_total_hits";
    public static final String TOTAL_SHARDS = "total_shards";
    public static final String HOST = "host";
    public static final String INSTANCE_ID = "instanceId";
    public static final String LEVEL = "level";
    public static final String DOC_TYPE = "doc_type";
    public static final String ES_RESOURCE_UID = "es_resourceUid";
    public static final String ES_RESOURCE_GROUP = "es_resource_group";
    public static final String ES_AVAILABLE_ZONE = "es_available_zone";

    public static final String bodySize = "bodySize";
    public static final String node = "node";
    public static final String remote = "remote";
    public static final String uri = "uri";
    public static final String regionId = "es_region";

    public static final String name = "name";
    public static final String status = "status";
    public static final String event_time = "event_time";
}
