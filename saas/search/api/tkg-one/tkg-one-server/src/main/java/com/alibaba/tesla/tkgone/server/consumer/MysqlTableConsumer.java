package com.alibaba.tesla.tkgone.server.consumer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.tkgone.server.common.Constant;
import com.alibaba.tesla.tkgone.server.common.MysqlHelper;
import com.alibaba.tesla.tkgone.server.common.Tools;
import com.alibaba.tesla.tkgone.server.domain.dto.ConsumerDto;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yangjinghua
 */
@Log4j
@Service
public class MysqlTableConsumer extends AbstractConsumer implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        int concurrentExecNum = consumerConfigService.getSingleConsumerMysqlTableConcurrentExecNum();
        int effectiveInterval = consumerConfigService.getMysqlTableEffectiveInterval();
        afterPropertiesSet(concurrentExecNum, effectiveInterval, ConsumerSourceType.mysqlTable);
    }

    @Override
    int consumerDataByConsumerDto(ConsumerDto consumerDto) throws Exception {
        consumerDto = new ConsumerDto(consumerMapper.selectByPrimaryKey(consumerDto.getId()));
        String host = consumerDto.getSourceInfoJson().getString("host");
        String port = consumerDto.getSourceInfoJson().getString("port");
        String db = consumerDto.getSourceInfoJson().getString("db");
        String username = consumerDto.getSourceInfoJson().getString("username");
        String password = consumerDto.getSourceInfoJson().getString("password");
        String sql = consumerDto.getSourceInfoJson().getString("sql");
        JSONArray flatKeys = consumerDto.getSourceInfoJson().getJSONArray("flatKeys");
        sql = Tools.processTemplateString(sql,
                JSONObject.parseObject(JSONObject.toJSONString(consumerDto)).getInnerMap());
        boolean isPartition = consumerDto.getSourceInfoJson().getBooleanValue("isPartition");

        MysqlHelper mysqlHelper = new MysqlHelper(host, port, db, username, password);
        String partition = isPartition ? Tools.currentDataString() : null;
        final int[] alreadyFetchDataSize = {0};
        ConsumerDto finalConsumerDto = consumerDto;
        log.info(String.format("consume data by consumer, start consumer: %s", consumerDto.getName()));
        mysqlHelper.executeQuery(sql, Constant.FETCH_DATA_SIZE, retList -> {
//            flat(retList, flatKeys);
            alreadyFetchDataSize[0] += saveToBackendStore(finalConsumerDto, retList,
                    finalConsumerDto.getImportConfigArray(), partition);
        });
        log.info(String.format("%s fetchData: %s", consumerDto.getName(), alreadyFetchDataSize[0]));
        return alreadyFetchDataSize[0];
    }

    private JSONObject flatJsonObject(String parentKey, JSONObject jsonObject) {
        JSONObject ret = new JSONObject();
        if (jsonObject != null && !jsonObject.isEmpty()) {
            for (String key : jsonObject.keySet()) {
                Object value = JSONObject.toJSON(jsonObject.get(key));
                if (value instanceof JSONObject) {
                    ret.putAll(flatJsonObject(parentKey + "_" + key, (JSONObject)value));
                } else {
                    ret.put(parentKey + "_" + key, value);
                }
            }
        } else {
            ret.put(parentKey, jsonObject);
        }
        return ret;
    }

    private void flat(List<JSONObject> retList, JSONArray flatKeys) {
        if (!CollectionUtils.isEmpty(flatKeys)) {
            for (JSONObject jsonObject : retList) {
                for (String flatKey : flatKeys.toJavaList(String.class)) {
                    try {
                        jsonObject.putAll(flatJsonObject(flatKey, JSONObject.parseObject(jsonObject.getString(flatKey))));
                    } catch (Exception e) {
                        log.warn(String.format("flat json failed key:%s, msg:%s", flatKey, e));
                    }
                }
            }
        }
    }
}
