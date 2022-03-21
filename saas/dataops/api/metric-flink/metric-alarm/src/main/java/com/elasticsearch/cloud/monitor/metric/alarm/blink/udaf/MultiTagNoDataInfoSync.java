package com.elasticsearch.cloud.monitor.metric.alarm.blink.udaf;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.elasticsearch.cloud.monitor.commons.rule.Rule;
import com.elasticsearch.cloud.monitor.metric.alarm.blink.udaf.MultiTagNoDataInfoSync.MultiTagNoDataInfo;
import com.elasticsearch.cloud.monitor.metric.common.sync.OssSync;
import com.elasticsearch.cloud.monitor.metric.common.sync.OssSync.OssSyncCallBack;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaoping
 * @date 2020/11/30
 */
@Slf4j
public class MultiTagNoDataInfoSync extends OssSyncCallBack<List<MultiTagNoDataInfo>> {
    private OssSync<List<MultiTagNoDataInfo>> ossSync;
    private volatile Map<String, List<Map<String, String>>> ruleToNodataLines = Maps.newConcurrentMap();

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public static Splitter splitter = Splitter.on(",");

    public MultiTagNoDataInfoSync(String ossEndpoint, String ossAccessKey, String ossAccessSecret, String bucket,
        String file) {
        ossSync = new OssSync<>(ossEndpoint, ossAccessKey, ossAccessSecret, bucket, file);
        ossSync.registerCallBack(this, new TypeToken<List<MultiTagNoDataInfo>>() {}.getType());
    }

    @Override
    public void update(List<MultiTagNoDataInfo> multiTagNoDataInfos) throws InterruptedException {
        log.error("no data info oss updated");
        Map<String, List<Map<String, String>>> tempRuleToNodataLines = Maps.newConcurrentMap();
        if (multiTagNoDataInfos != null) {
            for (MultiTagNoDataInfo info : multiTagNoDataInfos) {
                tempRuleToNodataLines.put(info.getRuleName(), info.getNoDataLines());
            }
        }
        lock.writeLock().lock();
        ruleToNodataLines = tempRuleToNodataLines;
        lock.writeLock().unlock();
    }

    @Setter
    @Getter
    public static class MultiTagNoDataInfo {
        private String ruleName;
        private List<Map<String, String>> noDataLines;
    }

    public void rewriteRule(Rule rule) {
        if (rule == null) {
            return;
        }
        lock.readLock().lock();
        Map<String, List<Map<String, String>>> tempRuleToNodataLines = ruleToNodataLines;
        lock.readLock().unlock();

        List<String> list = splitter.splitToList(rule.getAppName());
        String appName = "";
        if (list.size() > 0) {
            appName = list.get(0);
        }
        String ruleName = String.format("%s|%s", appName, rule.getName());
        if (!tempRuleToNodataLines.containsKey(ruleName)) {
            return;
        }

        if (rule.getNoDataCondition() != null) {
            rule.getNoDataCondition().setNoDataLines(ruleToNodataLines.get(ruleName));
        }

    }

    public void close() {
        if (ossSync != null) {
            ossSync.close();
        }
    }
}
