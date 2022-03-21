package com.elasticsearch.cloud.monitor.metric.alarm.blink.rules.udtf;

import com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf.HealthAlert;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeFunctionContext;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeListCollector;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple13;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;


/**
 * @author xingming.xuxm
 * @Date 2019-12-12
 */
public class HealthAlertTest {
    private HealthAlert udtf = new HealthAlert();

    private FakeListCollector<Tuple13<Integer, Integer, String, String, String, String, String, String, String, String, String, Long, String>> collector;

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
        labels.put("app_name", "sds");
//        labels.put("app_component_name", "demoapp");
        labels.put("app_component_instance_id", "app-378a57acb330bb56926d58cf9024a7b9");
        labels.put("app_instance_id", "app-378a57acb330bb56926d58cf9024a7b9");
        labels.put("app_id", "sreworks1");

        String exConfig = "{\"enable\": true, \"storage_days\": 7, \"granularity\": 1, \"alert_rule_config\": {\"duration\": 1, \"comparator\": \">\", \"math_abs\": false, \"times\": 2, \"thresholds\": {\"WARNING\": 2, \"CRITICAL\": 4 } }, \"notice_config\": {\"warning\": [], \"critical\": [] } }";
        udtf.eval("app-378a57acb330bb56926d58cf9024a7b9",11, "user_order_failed_cnt",
                labels, 1645946402463L, 8.0f, 1, "sreworks1", exConfig);
        udtf.eval("app-378a57acb330bb56926d58cf9024a7b9",11, "user_order_failed_cnt",
                labels, 1645946402463L, 8.0f, 1, "sreworks1", exConfig);
        if (collector.size() != 0) {
            Tuple13 out = collector.get(collector.size() - 1);
            System.out.println(out.toString());
        }
    }
}
