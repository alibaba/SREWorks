package com.elasticsearch.cloud.monitor.metric.alarm.blink.rules.udtf;

import com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf.HealthAlert;
import com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf.HealthFailure;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeFunctionContext;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeListCollector;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple13;
import org.apache.flink.api.java.tuple.Tuple9;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author xingming.xuxm
 * @Date 2019-12-12
 */
public class HealthFailureTest {
    private HealthFailure udtf = new HealthFailure();

    private FakeListCollector<Tuple9<Integer, String, String, Long, String, String, Long, Long, String>> collector;

    @Before
    public void setUp() throws Exception {
        RuntimeContext mockRuntimeContext = Mockito.mock(RuntimeContext.class);
        FakeFunctionContext context = new FakeFunctionContext(mockRuntimeContext);
        udtf.open(context);

        collector = new FakeListCollector<>();
        udtf.setCollector(collector);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void eval() throws Exception {

        Map<String, String> labels = new HashMap<>();
        labels.put("app_name", "test_5");
        labels.put("app_component_name", "demoapp");
        labels.put("app_component_instance_id", "app_component_instance_id_2");
        labels.put("app_instance_id", "app_instance_id_1");
        labels.put("app_id", "sreworks56");


        String exConfig = "{\"failure_level_rule\":{\"P0\":\"5m\",\"P1\":\"4m\",\"P2\":\"3m\",\"P3\":\"2m\",\"P4\":\"1m\"},\"ref_incident_def_id\":5}";
        udtf.eval(66666L,"appInstanceId", "componentInstanceId",
                1642487235293L, 0L, "测试原因", 6, "sreworks56", exConfig);
        if (collector.size() != 0) {
            Tuple9 out = collector.get(collector.size() - 1);
            System.out.println(out.toString());
        }
    }
}
