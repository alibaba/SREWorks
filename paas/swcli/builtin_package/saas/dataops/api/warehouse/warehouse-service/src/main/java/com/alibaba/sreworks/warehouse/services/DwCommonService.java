package com.alibaba.sreworks.warehouse.services;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.warehouse.operator.ESDocumentOperator;
import com.alibaba.sreworks.warehouse.operator.ESIndexOperator;
import com.alibaba.sreworks.warehouse.operator.ESLifecycleOperator;
import com.alibaba.sreworks.warehouse.operator.ESSearchOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2021/12/01 17:17
 */
@Slf4j
@Service
public class DwCommonService {
    @Autowired
    ESIndexOperator esIndexOperator;

    @Autowired
    ESDocumentOperator esDocumentOperator;

    @Autowired
    ESSearchOperator esSearchOperator;

    @Autowired
    ESLifecycleOperator esLifecycleOperator;

    protected int doFlushDatas(String alias, String index, Integer lifecycle, List<JSONObject> esDatas) throws Exception{
        if (CollectionUtils.isEmpty(esDatas)) {
            return 0;
        }

        log.info(String.format("====flush data, es_index:%s, es_index_alias:%s====", index, alias));
        if (!esIndexOperator.existIndex(index)) {
            // 创建索引
            createTableMeta(index, alias, lifecycle);
        }

        if (esDatas.size() > 1) {
            return esDocumentOperator.upsertBulkJSON(index, esDatas);
        } else {
            return esDocumentOperator.upsertJSON(index, esDatas.get(0));
        }
    }

    protected void createTableMeta(String tableName, String tableAlias, Integer lifecycle) throws Exception {
        // 生命周期管理
        log.info("====create lifecycle policy====");
        String policyName = esLifecycleOperator.createLifecyclePolicy(lifecycle);
        // TODO 按照数据类型做mapping映射
        log.info("====create index====");
        esIndexOperator.createIndexIfNotExist(tableName, tableAlias, policyName);
    }

    protected void updateTableLifecycle(String tableName, String tableAlias, Integer lifecycle) throws Exception {
        // 生命周期管理
        log.info("====create lifecycle policy====");
        String policyName = esLifecycleOperator.createLifecyclePolicy(lifecycle);

        log.info("====update index lifecycle====");
        esIndexOperator.updateIndexLifecyclePolicy(tableName, tableAlias, policyName);
    }

    protected JSONObject statsTable(String tableName, String tableAlias) {
        JSONObject stats = new JSONObject();

        String newestIndex = null;
        try {
            Set<String> indices = esIndexOperator.getIndicesByAlias(tableAlias);
            int partitionCount = indices.size();
            stats.put("partitionCount", partitionCount);

            List<String> indexList = new ArrayList<>(indices);
            newestIndex = indexList.get(indexList.size() - 1);
        } catch (Exception ex) {
            log.error(String.format("统计模型[table:%s, alias:%s]分区数异常, 详情:%s", tableName, tableAlias, ex.getMessage()));
        }

        try {
            long docCount = esSearchOperator.countDoc(tableName);
            stats.put("docCount", docCount);
        } catch (Exception ex) {
            log.error(String.format("统计模型[table:%s, alias:%s]]最新分区实例数异常, 详情:%s",tableName, tableAlias, ex.getMessage()));
            if (StringUtils.isNotEmpty(newestIndex)) {
                try {
                    long docCount = esSearchOperator.countDoc(newestIndex);
                    stats.put("docCount", docCount);
                } catch (Exception inEx) {
                    log.error(String.format("统计模型[table:%s, alias:%s]最新分区实例数异常, 详情:%s", tableName, tableAlias, inEx.getMessage()));
                }
            }
        }

        return stats;
    }
}
