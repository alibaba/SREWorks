package com.elasticsearch.cloud.monitor.metric.alarm.blink.rules.udtf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DurationAlarmDebugTest {
    private DurationAlarmDebug debugger;

    @Before
    public void setUp() throws Exception {
        DurationAlarmDebug.DurationAlarmConfig.DurationAlarmConfigBuilder builder = new DurationAlarmDebug.DurationAlarmConfig.DurationAlarmConfigBuilder();
        DurationAlarmDebug.DurationAlarmConfig config = builder.accessId("xxxxxx").accessKey("xxxxxx")
                .slsEndpoint("cn-beijing.log.aliyuncs.com").slsProject("elasticmointor-openserch-cn-beijing").slsLogstore("metric-agg")
                .ossEndpoint("oss-cn-beijing.aliyuncs.com").ossRulepath("oss://kmon-elm-bj/default/rules.json").build();

        debugger = new DurationAlarmDebug(config);
        debugger.init();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void eval() throws Exception {
        debugger.debug("tags: task_name=150022523_enhanced_join_realtime and rule_id: 1358",
                1584143640, 1584150960, 1584146640000L);
    }
}