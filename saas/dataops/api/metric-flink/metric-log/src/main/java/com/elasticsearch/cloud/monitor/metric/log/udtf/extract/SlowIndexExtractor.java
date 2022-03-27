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
public class SlowIndexExtractor {
    private static final Log log = LogFactory.getLog(SlowIndexExtractor.class);
    private Monitor monitor;
    private EsInfoSync esInfoSync;

    private final String TOOK_MILLIS = "took_millis";
    private final String TYPE = "type";
    private final String ID = "id";

    public SlowIndexExtractor(Monitor monitor, EsInfoSync esInfoSync) {
        this.monitor = monitor;
        this.esInfoSync = esInfoSync;
    }

    public String extract(String logContent, String timestampSecond, String esInstanceId, String ip,
        String level) throws Exception {
        List<String> contents = Util.indexSplitter.splitToList(logContent);
        if (contents == null || contents.size() < 4) {
            log.error(String
                .format("ExtractMetric invalid slow index log of content length, content %s instanceId %s",
                    logContent,
                    esInstanceId));
            return null;
        }

        String indexNameAndUUid = contents.get(2);
        String indexName = null;
        if ("]".equals(contents.get(1))) {
            indexNameAndUUid = contents.get(3);
        }
        String[] indexNameAndUUidArray = indexNameAndUUid.split("/");
        if (indexNameAndUUidArray.length == 1) {
            indexName = indexNameAndUUidArray[0].substring(1, indexNameAndUUidArray[0].length() - 1);
        } else if (indexNameAndUUidArray.length == 2) {
            indexName = indexNameAndUUidArray[0].substring(1);
        } else if (indexNameAndUUidArray.length > 2) {
            int suffixLength = indexNameAndUUidArray[indexNameAndUUidArray.length - 1].length() + 1;
            indexName = indexNameAndUUid.substring(1, indexNameAndUUid.length() - suffixLength);
        }
        if (StringUtils.isEmpty(indexName)) {
            log.error(String
                .format("ExtractMetric invalid slow index log of indexNameAndUUid %s, content %s instanceId %s",
                    indexNameAndUUid, logContent, esInstanceId));
            return null;
        }

        Double tookMillis = null;
        String docType = null;
        String source = "";
        for (int i = 3; i < contents.size(); i++) {

            String content = contents.get(i);
            if (content.startsWith(TOOK_MILLIS)) {
                tookMillis = Util.getDigitFromString(content, TOOK_MILLIS.length() + 1, content.length() - 1);

            } else if (content.startsWith(TYPE)) {
                docType = content.substring(TYPE.length() + 1, content.length() - 2);
            } else if (content.startsWith(ID)) {
                source = Util.joiner.join(contents.subList(i, contents.size()));
            }
        }
        if (tookMillis == null) {
            log.error(String
                .format(
                    "ExtractMetric invalid slow index log of took millis %s, content %s "
                        + "instanceId %s", tookMillis, logContent, esInstanceId));
            return null;
        }
        if (StringUtils.isEmpty(source)) {
            log.error(String
                .format(
                    "ExtractMetric invalid slow index log of source is empty , content %s "
                        + "instanceId %s", logContent, esInstanceId));
            return null;
        }

        Map<String, Object> data = Maps.newHashMap();
        data.put(Constant.INDEX_NAME, indexName);

        data.put(Constant.CONTENT, source);
        data.put(Constant.INDEX_TIME_MS, tookMillis);
        data.put(Constant.HOST, ip);
        data.put(Constant.INSTANCE_ID, esInstanceId);
        data.put(Constant.LEVEL, level);
        if (StringUtils.isNotEmpty(docType)) {
            data.put(Constant.DOC_TYPE, docType);
        }
        if (esInfoSync != null) {
            if (!esInfoSync.addBizTag(esInstanceId, ip, data)) {
                return null;
            }
        }
        String dataString = Util.gson.toJson(data);
        return dataString;
    }
}
