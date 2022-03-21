package com.elasticsearch.cloud.monitor.metric.log.udtf.extract;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author xiaoping
 * @date 2021/5/15
 */
public class SlowIndexExtractorTest {

    @Test
    public void testExtract() throws Exception {
        SlowIndexExtractor slowIndexExtractor=new SlowIndexExtractor(null,null);
        String content="[index.indexing.slowlog.index] [PSMgx2k] [.monitoring-es-6-2021.05.13/RJhf27KHTF2B4NscdM_Ytg]"
            + " took[32.7ms], took_millis[32], type[doc], id[yws_ZXkBz1xKQj5FNOaX], routing[], "
            + "source[{\"cluster_uuid\":\"41VGOGvmRYuB0BU2QW8jsA\",\"timestamp\":\"2021-05-13T10:21:51.631Z\","
            + "\"interval_ms\":10000,\"type\":\"node_stats\",\"source_node\":{\"uuid\":\"lFVBcPk9SLCg2dLiZVg98w\","
            + "\"host\":\"172.31.179.81\",\"transport_address\":\"172.31.179.81:9300\",\"ip\":\"172.31.179.81\","
            + "\"name\":\"lFVBcPk\",\"timestamp\":\"2021-05-13T10:21:51.631Z\"},"
            + "\"node_stats\":{\"node_id\":\"lFVBcPk9SLCg2dLiZVg98w\",\"node_master\":false,\"mlockall\":true,"
            + "\"indices\":{\"docs\":{\"count\":0},\"store\":{\"size_in_bytes\":0},\"indexing\":{\"index_total\":0,"
            + "\"index_time_in_millis\":0,\"throttle_time_in_millis\":0},\"search\":{\"query_total\":0,"
            + "\"query_time_in_millis\":0},\"refresh\":{\"total\":0,\"total_time_in_millis\":0},"
            + "\"query_cache\":{\"memory_size_in_bytes\":0,\"hit_count\":0,\"miss_count\":0,\"evictions\":0},"
            + "\"fielddata\":{\"memory_size_in_bytes\":0,\"evictions\":0},\"segments\":{\"count\":0,"
            + "\"memory_in_bytes\":0,\"terms_memory_in_bytes\":0,\"stored_fields_memory_in_bytes\":0,"
            + "\"term_vectors_memory_in_bytes\":0,\"norms_memory_in_bytes\":0,\"points_memory_in_bytes\":0,"
            + "\"doc_values_memory_in_bytes\":0,\"index_writ]";
      String result= slowIndexExtractor.extract(content,"123","es-xx","1.1.1.1","info");
      System.out.println(result);

    }

}
