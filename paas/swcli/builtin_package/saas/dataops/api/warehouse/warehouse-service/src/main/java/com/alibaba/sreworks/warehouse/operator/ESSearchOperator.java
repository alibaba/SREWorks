package com.alibaba.sreworks.warehouse.operator;

import com.alibaba.sreworks.warehouse.common.client.ESClient;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * ES查询操作服务类
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2021/11/10 20:52
 */
@Service
@Slf4j
public class ESSearchOperator {

    @Autowired
    ESClient esClient;

    public long countDoc(String index) throws Exception {
        CountRequest countRequest = new CountRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        countRequest.source(searchSourceBuilder);

        RestHighLevelClient hlClient = esClient.getHighLevelClient();
        CountResponse countResponse = hlClient.count(countRequest, RequestOptions.DEFAULT);

        return countResponse.getCount();
    }
}
