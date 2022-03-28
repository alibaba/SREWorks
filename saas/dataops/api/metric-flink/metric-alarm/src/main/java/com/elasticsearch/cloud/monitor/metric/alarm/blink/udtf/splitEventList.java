package com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf;

import com.elasticsearch.cloud.monitor.commons.utils.StringUtils;
import com.elasticsearch.cloud.monitor.metric.alarm.blink.utils.AlarmEvent;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.table.functions.TableFunction;

import java.util.List;

/**
 * @author xingming.xuxm
 * @Date 2019-11-27
 */
public class splitEventList extends TableFunction<Tuple9<String, String, String, String, String, String, String, String, String>> {
    public void eval(List<AlarmEvent> events) throws Exception {
        if (events == null) {
            return;
        }

        for (AlarmEvent event : events) {
            collect(Tuple9.of(
                    event.getService(),
                    event.getSource(),
                    StringUtils.join(event.getTags(), ","),
                    event.getText(),
                    event.getTitle(),
                    event.getType(),
                    String.valueOf(event.getTime()), event.getGroup(), event.getUid()));
        }
    }
}
