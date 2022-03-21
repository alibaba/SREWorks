package com.elasticsearch.cloud.monitor.metric.log.udtf;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaoping
 * @date 2021/3/10
 */
public class ExtractMetricTest {

    @Test
    public void testSlowSearch() {
        ExtractMetricMock extractMetricMock = new ExtractMetricMock();
        String logContent = "[i.s.s.query              ] [es-cn-zz11zxsy8000b20mi-80cb365e-0001] [company][1] took[113"
            + ".2ms], took_millis[113], total_hits[0 hits], types[], stats[], search_type[QUERY_THEN_FETCH], "
            + "total_shards[10], source[{\"from\":0,\"size\":100,"
            + "\"query\":{\"bool\":{\"filter\":[{\"term\":{\"client_id\":{\"value\":\"23695\",\"boost\":1.0}}}],"
            + "\"must_not\":[{\"terms\":{\"_id\":[\"43625967708\"],\"boost\":1.0}}],"
            + "\"should\":[{\"regexp\":{\"homepage.keyword\":{\"value\":\".*riina.ca.*\",\"flags_value\":65535,"
            + "\"max_determinized_states\":10000,\"boost\":1.0}}}],\"adjust_pure_negative\":true,"
            + "\"minimum_should_match\":\"1\",\"boost\":1.0}},\"sort\":[]}], id[],";
        extractMetricMock.eval(logContent, "1", "es-cnxx", "1.1.1.1", "INFO", "search");
        Set<String> metrics = extractMetricMock.getMetrics();
        for (String metric : metrics) {
            System.out.println(metric);
        }
        Assert.assertTrue(metrics.contains("elasticsearch-log.slow.search.count 1000 1 instance_id=es-cnxx ip=1.1.1"
            + ".1 index=company shard=1 search_type=QUERY_THEN_FETCH level=INFO slow_log_type=query"));
        Assert.assertTrue(metrics.contains("elasticsearch-log.slow.search.total_hits 1000 0.0 instance_id=es-cnxx "
            + "ip=1.1.1.1 index=company shard=1 search_type=QUERY_THEN_FETCH level=INFO slow_log_type=query"));
        Assert.assertTrue(metrics.contains("elasticsearch-log.slow.search.time 1000 113.0 instance_id=es-cnxx ip=1.1"
            + ".1.1 index=company shard=1 search_type=QUERY_THEN_FETCH level=INFO slow_log_type=query"));
    }

    @Test
    public void testSlowSearch1() {
        ExtractMetricMock extractMetricMock = new ExtractMetricMock();
        String logContent = "[i.s.s.query] [es-cn-zz11zxsy8000b20mi-80cb365e-0001] [company][12] took[113"
            + ".2ms], took_millis[113], total_hits[10], types[], stats[], search_type[QUERY_THEN_FETCH], "
            + "total_shards[10], source[{\"from\":0,\"size\":100,"
            + "\"query\":{\"bool\":{\"filter\":[{\"term\":{\"client_id\":{\"value\":\"23695\",\"boost\":1.0}}}],"
            + "\"must_not\":[{\"terms\":{\"_id\":[\"43625967708\"],\"boost\":1.0}}],"
            + "\"should\":[{\"regexp\":{\"homepage.keyword\":{\"value\":\".*riina.ca.*\",\"flags_value\":65535,"
            + "\"max_determinized_states\":10000,\"boost\":1.0}}}],\"adjust_pure_negative\":true,"
            + "\"minimum_should_match\":\"1\",\"boost\":1.0}},\"sort\":[]}], id[],";
        extractMetricMock.eval(logContent, "1", "es-cnxx", "1.1.1.1", "INFO", "search");
        Set<String> metrics = extractMetricMock.getMetrics();
        for (String metric : metrics) {
            System.out.println(metric);
        }
        Assert.assertTrue(metrics.contains("elasticsearch-log.slow.search.count 1000 1 instance_id=es-cnxx ip=1.1.1"
            + ".1 index=company shard=12 search_type=QUERY_THEN_FETCH level=INFO slow_log_type=query"));
        Assert.assertTrue(metrics.contains("elasticsearch-log.slow.search.total_hits 1000 10.0 instance_id=es-cnxx "
            + "ip=1.1.1.1 index=company shard=12 search_type=QUERY_THEN_FETCH level=INFO slow_log_type=query"));
        Assert.assertTrue(metrics.contains("elasticsearch-log.slow.search.time 1000 113.0 instance_id=es-cnxx ip=1.1"
            + ".1.1 index=company shard=12 search_type=QUERY_THEN_FETCH level=INFO slow_log_type=query"));
    }

    @Test
    public void testSlowIndex() {
        ExtractMetricMock extractMetricMock = new ExtractMetricMock();
        String logContent = "[i.i.s.index              ] [es-cn-09k21wbq5000hxi96-13ffa7dd-0004] [.monitoring-es-7-2021"
            + ".03.11/5dQGNSlqSZCv5LEeG8RwJQ] took[113.9ms], took_millis[113], type[_doc], "
            + "id[gbaPnj1qRdikyQWz0Fpd-Q:ykwLqHc3SEWWtVOh8OS_uQ:instance_jvm_thread_peak_count-20210311:0:r], "
            + "routing[], source[{\"cluster_uuid\":\"rLxMsUHLTO-ZPYyqcUPXJg\",\"timestamp\":\"2021-03-11T06:34:13"
            + ".405Z\",\"interval_ms\":10000,\"type\":\"shards\","
            + "\"source_node\":{\"uuid\":\"ykwLqHc3SEWWtVOh8OS_uQ\",\"host\":\"10.15.18.199\","
            + "\"transport_address\":\"10.15.18.199:9300\",\"ip\":\"10.15.18.199\","
            + "\"name\":\"es-cn-09k21wbq5000hxi96-13ffa7dd-0005\",\"timestamp\":\"2021-03-11T06:34:13.053Z\"},"
            + "\"state_uuid\":\"gbaPnj1qRdikyQWz0Fpd-Q\",\"shard\":{\"state\":\"STARTED\",\"primary\":false,"
            + "\"node\":\"ykwLqHc3SEWWtVOh8OS_uQ\",\"relocating_node\":null,\"shard\":0,"
            + "\"index\":\"instance_jvm_thread_peak_count-20210311\"}}]";
        extractMetricMock.eval(logContent, "1", "es-cnxx", "1.1.1.1", "INFO", "index");
        Set<String> metrics = extractMetricMock.getMetrics();
        for (String metric : metrics) {
            System.out.println(metric);
        }
        Assert.assertTrue(metrics.contains(
            "elasticsearch-log.slow.index.count 1000 1 instance_id=es-cnxx ip=1.1.1.1 index=.monitoring-es-7-2021.03"
                + ".11 level=INFO type=_doc"));
        Assert.assertTrue(metrics.contains(
            "elasticsearch-log.slow.index.time 1000 113.0 instance_id=es-cnxx ip=1.1.1.1 index=.monitoring-es-7-2021.03.11 level=INFO type=_doc"));
    }

    @Test
    public void testSlowIndex1() {
        ExtractMetricMock extractMetricMock = new ExtractMetricMock();
        String logContent = "[index.indexing.slowlog.index] [es-cn-st21za1l90001v7ie-b191f2a6-0003] "
            + "[study_task/TZeWxzAITFyACPp_ELCwmA] took[104.7ms], took_millis[104], type[_doc], id[1795373], "
            + "routing[], source[{\"first_week_lesson\":0,\"timestamp\":\"2021-03-11T14:34:15.423Z\","
            + "\"type\":\"jdbc\",\"id\":1795373,\"work_type_5\":0,\"work_type_5_time\":0,\"work_type_all_time\":0,"
            + "\"second_week_lesson\":0,\"work_type_2\":0,\"work_type_2_time\":0,\"third_week_lesson\":0,"
            + "\"work_type_4\":0,\"work_type_4_time\":0,\"work_type_1_time\":0,\"work_type_1\":0,\"work_type_3\":0,"
            + "\"work_type_3_time\":0,\"last_cover_days\":0,\"last_note_time\":0,\"residue_lesson_count\":1000,"
            + "\"last_lesson_time\":0,\"last_lesson_course_title\":\"\",\"last_lesson_course_id\":0,"
            + "\"master_id\":358,\"sex\":2,\"mastername\":\"358\",\"master_name\":\"358\",\"master\":358,"
            + "\"teacher\":null,\"teamid\":1,\"age\":6,\"username\":\"呵呵呵\",\"teamname\":\"eOMtThyhVNLWUZNRcBaQKxI\","
            + "\"work_type_6\":0,\"work_type_6_time\":0,\"work_type_7_time\":0,\"work_type_7\":0,\"flowstatus\":6}]";
        extractMetricMock.eval(logContent, "1", "es-cnxx", "1.1.1.1", "INFO", "index");
        Set<String> metrics = extractMetricMock.getMetrics();
        for (String metric : metrics) {
            System.out.println(metric);
        }
        Assert.assertTrue(metrics.contains(
            "elasticsearch-log.slow.index.time 1000 104.0 instance_id=es-cnxx ip=1.1.1.1 index=study_task level=INFO type=_doc"));
        Assert.assertTrue(metrics.contains(
            "elasticsearch-log.slow.index.count 1000 1 instance_id=es-cnxx ip=1.1.1.1 index=study_task level=INFO type=_doc"));
    }

    @Test
    public void testSlowIndex2() {
        ExtractMetricMock extractMetricMock = new ExtractMetricMock();
        String logContent = "[index.indexing.slowlog.index] [es-cn-st21za1l90001v7ie-b191f2a6-0003] "
            + "[study_task] took[104.7ms], took_millis[104], type[_doc], id[1795373], "
            + "routing[], source[{\"first_week_lesson\":0,\"timestamp\":\"2021-03-11T14:34:15.423Z\","
            + "\"type\":\"jdbc\",\"id\":1795373,\"work_type_5\":0,\"work_type_5_time\":0,\"work_type_all_time\":0,"
            + "\"second_week_lesson\":0,\"work_type_2\":0,\"work_type_2_time\":0,\"third_week_lesson\":0,"
            + "\"work_type_4\":0,\"work_type_4_time\":0,\"work_type_1_time\":0,\"work_type_1\":0,\"work_type_3\":0,"
            + "\"work_type_3_time\":0,\"last_cover_days\":0,\"last_note_time\":0,\"residue_lesson_count\":1000,"
            + "\"last_lesson_time\":0,\"last_lesson_course_title\":\"\",\"last_lesson_course_id\":0,"
            + "\"master_id\":358,\"sex\":2,\"mastername\":\"358\",\"master_name\":\"358\",\"master\":358,"
            + "\"teacher\":null,\"teamid\":1,\"age\":6,\"username\":\"呵呵呵\",\"teamname\":\"eOMtThyhVNLWUZNRcBaQKxI\","
            + "\"work_type_6\":0,\"work_type_6_time\":0,\"work_type_7_time\":0,\"work_type_7\":0,\"flowstatus\":6}]";
        extractMetricMock.eval(logContent, "1", "es-cnxx", "1.1.1.1", "INFO", "index");
        Set<String> metrics = extractMetricMock.getMetrics();
        for (String metric : metrics) {
            System.out.println(metric);
        }
        Assert.assertTrue(metrics.contains(
            "elasticsearch-log.slow.index.time 1000 104.0 instance_id=es-cnxx ip=1.1.1.1 index=study_task level=INFO type=_doc"));
        Assert.assertTrue(metrics.contains(
            "elasticsearch-log.slow.index.count 1000 1 instance_id=es-cnxx ip=1.1.1.1 index=study_task level=INFO type=_doc"));
    }

    @Test
    public void testSlowIndex3() {
        ExtractMetricMock extractMetricMock = new ExtractMetricMock();
        String logContent = "[index.indexing.slowlog.index] [es-cn-st21za1l90001v7ie-b191f2a6-0003] "
            + "[study_tas/k/TZeWxzAITFyACPp_ELCwmA] took[104.7ms], took_millis[104], type[_doc], id[1795373], "
            + "routing[], source[{\"first_week_lesson\":0,\"timestamp\":\"2021-03-11T14:34:15.423Z\","
            + "\"type\":\"jdbc\",\"id\":1795373,\"work_type_5\":0,\"work_type_5_time\":0,\"work_type_all_time\":0,"
            + "\"second_week_lesson\":0,\"work_type_2\":0,\"work_type_2_time\":0,\"third_week_lesson\":0,"
            + "\"work_type_4\":0,\"work_type_4_time\":0,\"work_type_1_time\":0,\"work_type_1\":0,\"work_type_3\":0,"
            + "\"work_type_3_time\":0,\"last_cover_days\":0,\"last_note_time\":0,\"residue_lesson_count\":1000,"
            + "\"last_lesson_time\":0,\"last_lesson_course_title\":\"\",\"last_lesson_course_id\":0,"
            + "\"master_id\":358,\"sex\":2,\"mastername\":\"358\",\"master_name\":\"358\",\"master\":358,"
            + "\"teacher\":null,\"teamid\":1,\"age\":6,\"username\":\"呵呵呵\",\"teamname\":\"eOMtThyhVNLWUZNRcBaQKxI\","
            + "\"work_type_6\":0,\"work_type_6_time\":0,\"work_type_7_time\":0,\"work_type_7\":0,\"flowstatus\":6}]";
        extractMetricMock.eval(logContent, "1", "es-cnxx", "1.1.1.1", "INFO", "index");
        Set<String> metrics = extractMetricMock.getMetrics();
        for (String metric : metrics) {
            System.out.println(metric);
        }
        Assert.assertTrue(metrics.contains(
            "elasticsearch-log.slow.index.time 1000 104.0 instance_id=es-cnxx ip=1.1.1.1 index=study_tas/k level=INFO type=_doc"));
        Assert.assertTrue(metrics.contains(
            "elasticsearch-log.slow.index.count 1000 1 instance_id=es-cnxx ip=1.1.1.1 index=study_tas/k level=INFO type=_doc"));
    }

}
