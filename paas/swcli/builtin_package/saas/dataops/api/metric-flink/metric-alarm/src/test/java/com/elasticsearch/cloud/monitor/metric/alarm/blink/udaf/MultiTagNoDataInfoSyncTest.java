package com.elasticsearch.cloud.monitor.metric.alarm.blink.udaf;

import java.util.List;
import java.util.Map;

import com.elasticsearch.cloud.monitor.metric.alarm.blink.udaf.MultiTagNoDataInfoSync.MultiTagNoDataInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author xiaoping
 * @date 2020/11/30
 */
public class MultiTagNoDataInfoSyncTest {

    @Test
    public void testPrintNoDataInfo(){
        List<MultiTagNoDataInfo> infos= Lists.newArrayList();
        Map<String,String> line1= Maps.newHashMap();
        line1.put("cluster","enhanced_join_rt");
        line1.put("env","out_beijing");

        Map<String,String> line2= Maps.newHashMap();
        line2.put("cluster","enhanced_join_rt");
        line2.put("env","out_beijing1");

        Map<String,String> line3= Maps.newHashMap();
        line3.put("cluster","enhanced_xiaoping");
        line3.put("env","out_beijing");
        MultiTagNoDataInfo info=new MultiTagNoDataInfo();
        info.setRuleName("xiaoping_test_multi_tag");
        info.setNoDataLines(Lists.newArrayList(line1,line2,line3));

        infos.add(info);

        System.out.println(new Gson().toJson(infos));

    }

}
