package com.elasticsearch.cloud.monitor.metric.alarm.blink.rules.udtf;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.common.QueriedLog;
import com.aliyun.openservices.log.response.GetLogsResponse;
import com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf.DurationAlarm;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeFunctionContext;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeListCollector;
import com.elasticsearch.cloud.monitor.metric.common.constant.Constants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Builder;
import lombok.Data;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple9;
import org.mockito.Mockito;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DurationAlarmDebug {

    RuntimeContext mockRuntimeContext = Mockito.mock(RuntimeContext.class);
    private FakeFunctionContext context = new FakeFunctionContext(mockRuntimeContext);
    private Client slsClient;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DurationAlarm udtf = new DurationAlarm();
    private FakeListCollector<Tuple9<String, String, String, String, String, String, Long, String, String>> collector = new FakeListCollector<>();
    private DurationAlarmConfig config;

    public DurationAlarmDebug(DurationAlarmConfig config) {
        this.config = config;
        context.setJobParameter(Constants.RULE_PATH, String.format("oss://%s:%s@%s/%s",
                config.getAccessId(), config.getAccessKey(), config.getOssEndpoint(), config.getOssRulepath().substring("oss://".length())));

//        context.setJobParameter(AlarmConstants.SLS_CONFIG_ENDPOINT, config.getSlsEndpoint());
//        context.setJobParameter(AlarmConstants.SLS_CONFIG_ACCESSID, config.getAccessId());
//        context.setJobParameter(AlarmConstants.SLS_CONFIG_ACCESSKEY, config.getAccessKey());
//        context.setJobParameter(AlarmConstants.SLS_CONFIG_PROJECT, config.getSlsProject());
//        context.setJobParameter(AlarmConstants.SLS_CONFIG_LOGSTORE, config.getSlsLogstore());

        slsClient = new Client(config.getSlsEndpoint(), config.getAccessId(), config.getAccessKey());
    }

    public void init() throws Exception {
        udtf.open(context);
        udtf.setCollector(collector);
    }

    public void debug(String query, int from, int to, long breakTimestamp) throws Exception {
        GetLogsResponse res = slsClient.GetLogs(
                config.getSlsProject(),
                config.getSlsLogstore(),
                from, to,
                "",
                query,
                10000, 0, false);

        int resultCnt = 0;
        JsonParser parser = new JsonParser();
        ArrayList<QueriedLog> logs = res.GetLogs();
        for (QueriedLog log : logs) {
            LogItem item = log.GetLogItem();
            JsonObject o = parser.parse(item.ToJsonString()).getAsJsonObject();
            Long rule_id = o.get("rule_id").getAsLong();
            String metric_name = o.get("metric_name").getAsString();
            long timestamp = o.get("timestamp").getAsLong();
            double metric_value = o.get("metric_value").getAsDouble();
            String tags = o.get("tags").getAsString();
            String granularity = o.get("granularity").getAsString();

            if (timestamp == breakTimestamp) {
                System.out.println("break for debug");
            }

            udtf.eval(rule_id, metric_name, timestamp, metric_value, tags, granularity);
            if (collector.size() != resultCnt) {
                Tuple9 out = collector.get(collector.size() - 1);
                System.out.println("alarm event------------------>");
                System.out.println(out.toString());

                resultCnt = collector.size();
            }
            System.out.println(item.ToJsonString());
            System.out.println(sdf.format(new Date(timestamp)));
            System.out.println("==================================");
        }
    }

    @Builder
    @Data
    public static class DurationAlarmConfig {
        private String accessId;
        private String accessKey;
        private String slsEndpoint;
        private String slsProject;
        private String slsLogstore;
        private String ossEndpoint;
        private String ossRulepath;
    }
}
