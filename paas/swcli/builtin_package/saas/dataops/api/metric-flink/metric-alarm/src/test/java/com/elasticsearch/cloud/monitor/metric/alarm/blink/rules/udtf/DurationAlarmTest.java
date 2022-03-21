package com.elasticsearch.cloud.monitor.metric.alarm.blink.rules.udtf;

import com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf.DurationAlarm;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeFunctionContext;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeListCollector;
import com.elasticsearch.cloud.monitor.metric.common.constant.Constants;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple9;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * @author xingming.xuxm
 * @Date 2019-12-12
 */
public class DurationAlarmTest {
    private DurationAlarm udtf = new DurationAlarm();

    private FakeListCollector<Tuple9<String, String, String, String, String, String, Long, String, String>> collector;

    @Before
    public void setUp() throws Exception {
        RuntimeContext mockRuntimeContext = Mockito.mock(RuntimeContext.class);
        FakeFunctionContext context = new FakeFunctionContext(mockRuntimeContext);

        String endpoint = "http://data-minio.ca221ae8860d9421688e59c8ab45c8b21.cn-hangzhou.alicontainer.com/";
        String ak = "admin";
        String sk = "password";
        String bucket = "metric-rules";

        context.setJobParameter(Constants.RULE_MINIO_ENDPOINT, endpoint);
        context.setJobParameter(Constants.RULE_MINIO_ACCESS_KEY, ak);
        context.setJobParameter(Constants.RULE_MINIO_SECRET_KEY, sk);
        context.setJobParameter(Constants.RULE_MINIO_BUCKET, bucket);
         context.setJobParameter(Constants.RULE_MINIO_FILE, "sreworks/rules.json");

        udtf.open(context);

        collector = new FakeListCollector<>();
        udtf.setCollector(collector);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void eval() throws Exception {
        udtf.eval(10001L, "pod_cpu_usage", 1631533140000L, 0.0000001, "service.type=kubernetes,metricset.name=pod,kubernetes.pod.name=dev-sreworks38-mall-7466869dcc-2mmsd", "1m");
        if (collector.size() != 0) {
            Tuple9 out = collector.get(collector.size() - 1);
            System.out.println(out.toString());
        }
    }
}
