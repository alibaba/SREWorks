package com.elasticsearch.cloud.monitor.metric.log.udtf.extract;

import com.elasticsearch.cloud.monitor.metric.log.common.Constant;
import com.elasticsearch.cloud.monitor.metric.log.common.Util;
import com.elasticsearch.cloud.monitor.metric.log.udtf.sync.EsInfoSync;
import com.google.common.collect.Maps;
import com.opensearch.cobble.monitor.Monitor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * @author xiaoping
 * @date 2021/3/31
 */
public class SlowSearchExtractor {
    private static final Log log = LogFactory.getLog(SlowSearchExtractor.class);
    private Monitor monitor;
    private EsInfoSync esInfoSync;

    private final String TOOK_MILLIS = "took_millis";
    private final String TOTAL_HITS = "total_hits";
    private final String SEARCH_TYPE = "search_type";
    private final String TOTAL_SHARD = "total_shards";
    private final String SOURCE = "source";

    public SlowSearchExtractor(Monitor monitor, EsInfoSync esInfoSync) {
        this.monitor = monitor;
        this.esInfoSync = esInfoSync;
    }

    public String extract(String logContent, String timestampSecond, String esInstanceId, String ip,
        String level) throws Exception {
        List<String> contents = Util.searchSplitter.splitToList(logContent);
        if (contents == null || contents.size() < 4) {
            log.error(String
                .format("ExtractMetric invalid slow search log of content length, content %s instanceId %s",
                    logContent,
                    esInstanceId));
            return null;
        }

        String slowSearchType;
        String indexName;
        String shardId;
        if (contents.get(0).contains(".query")) {
            slowSearchType = "query";
        } else if (contents.get(0).contains(".fetch")) {
            slowSearchType = "fetch";
        } else {
            log.error(String
                .format("ExtractMetric invalid slow search log of slowSearchType, content %s instanceId %s",
                    logContent,
                    esInstanceId));
            return null;

        }
        String[] indexAndShard = contents.get(2).split("]\\[");
        if (indexAndShard.length != 2) {
            indexAndShard = contents.get(3).split("]\\[");
        }
        if (indexAndShard.length != 2) {
            log.error(String
                .format("ExtractMetric invalid slow search log of index and shard %s, content %s instanceId %s",
                    Util.gson.toJson(indexAndShard), logContent, esInstanceId));
            return null;
        }
        indexName = indexAndShard[0].substring(1);
        shardId = indexAndShard[1].substring(0, indexAndShard[1].length() - 1);
        Double tookMillis = null;
        Double totalHits = null;
        String searchType = null;
        Double totalShard = null;
        String source = null;
        for (int i = 3; i < contents.size(); i++) {

            String content = contents.get(i);
            if (content.startsWith(TOOK_MILLIS)) {
                tookMillis = Util.getDigitFromString(content, TOOK_MILLIS.length() + 1, content.length() - 1);

            } else if (content.startsWith(TOTAL_HITS)) {
                totalHits = Util.getDigitFromString(content, TOTAL_HITS.length() + 1, content.length() - 1);

            } else if (content.startsWith(SEARCH_TYPE)) {
                searchType = content.substring(SEARCH_TYPE.length() + 1, content.length() - 2);

            } else if (content.startsWith(TOTAL_SHARD)) {
                totalShard = Double.parseDouble(content.substring(TOTAL_SHARD.length() + 1, content.length() - 2));
            } else if (content.startsWith(SOURCE)) {
                source = Util.joiner.join(contents.subList(i, contents.size())).substring(SOURCE.length() + 1);
            }
        }
        if (StringUtils.isEmpty(source)) {
            log.error(String
                .format(
                    "ExtractMetric source is empty,  content %s "
                        + "instanceId %s, split contents %s, contents length %s", logContent, esInstanceId,
                    Util.gson.toJson(contents), contents.size()));
        }
        if (tookMillis == null || StringUtils.isEmpty(searchType)) {
            log.error(String
                .format(
                    "ExtractMetric invalid slow search log of took %s   and search type %s, content %s "
                        + "instanceId %s", tookMillis, searchType, logContent, esInstanceId));
            return null;
        }

        Map<String, Object> data = Maps.newHashMap();
        data.put(Constant.INDEX_NAME, indexName);
        data.put(Constant.SHARD_ID, shardId);
        data.put(Constant.SEARCH_TYPE, searchType);
        data.put(Constant.SLOW_SEARCH_LOG_TYPE, slowSearchType);
        data.put(Constant.CONTENT, source);
        data.put(Constant.SEARCH_TIME_MS, tookMillis);
        data.put(Constant.SEARCH_TOTAL_HITS, totalHits);
        data.put(Constant.TOTAL_SHARDS, totalShard);
        data.put(Constant.HOST, ip);
        data.put(Constant.INSTANCE_ID, esInstanceId);
        data.put(Constant.LEVEL, level);
        if (esInfoSync != null) {
            if(!esInfoSync.addBizTag(esInstanceId, ip, data)){
                return null;
            }
        }
        String dataString = Util.gson.toJson(data);
        return dataString;
    }
}
