package com.elasticsearch.cloud.monitor.metric.common.rule.constant;

import com.taobao.kmonitor.sink.flume.FlumeSink;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaoping
 * @date 2019/11/22
 */
@Slf4j
public class SinkTypeTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testPrint() throws JSONException {
        log.info(FlumeSink.class.getName());
    }
}