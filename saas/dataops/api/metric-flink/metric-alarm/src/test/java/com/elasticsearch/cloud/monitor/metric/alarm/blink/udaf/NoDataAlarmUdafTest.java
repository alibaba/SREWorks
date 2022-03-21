package com.elasticsearch.cloud.monitor.metric.alarm.blink.udaf;

import com.elasticsearch.cloud.monitor.metric.alarm.blink.utils.AlarmEvent;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeFunctionContext;
import com.elasticsearch.cloud.monitor.metric.common.constant.Constants;
import com.google.common.io.Resources;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 * @author xingming.xuxm
 * @Date 2019-11-28
 */
public class NoDataAlarmUdafTest {

    private FakeFunctionContext context;

    @Before
    public void setUp() throws Exception {
        String rule = Resources.getResource("no_data").getPath();

        RuntimeContext mockRuntimeContext = Mockito.mock(RuntimeContext.class);
        context = new FakeFunctionContext(mockRuntimeContext);
        context.setJobParameter(Constants.RULE_PATH, rule);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
        NoDataAlarm udaf = new NoDataAlarm();
        udaf.open(context);
        NoDataAlarm.NoDataAlarmAccumulator acc = udaf.createAccumulator();

        {
            udaf.accumulate(acc, 1, System.currentTimeMillis(), "default", null);
            List<AlarmEvent> out = udaf.getValue(acc);
            assertTrue(out.size() == 1);
        }
        {
            udaf.accumulate(acc, 96191, System.currentTimeMillis(), "default", null);
            List<AlarmEvent> out = udaf.getValue(acc);
            assertTrue(out.size() == 0);
        }
    }
}
