package com.elasticsearch.cloud.monitor.metric.common.blink.utils;

import com.google.common.collect.Lists;
import org.junit.Test;

/**
 * @author xiaoping
 * @date 2021/6/11
 */
public class FilterTest {

    @Test
    public void test() {
        boolean a = true;
        boolean b = true;
        boolean c = false;
        System.out.println(a ^ b);
        System.out.println(a ^ c);
    }

    @Test
    public void testFilter() {
        Filter.setList(Lists.newArrayList("hologram.*.cpu_percent","hologram.fe.holo_query_qps"));
       System.out.println( Filter.match("aa"));
        System.out.println( Filter.match("hologram.aa.cpu_percent"));
        System.out.println( Filter.match("hologram.aa.bb.cpu_percent1"));
        System.out.println( Filter.match("hologram.fe.holo_query_qps"));
        System.out.println( Filter.match("hologram.fe.holo_query_qpsaa"));

    }

}
