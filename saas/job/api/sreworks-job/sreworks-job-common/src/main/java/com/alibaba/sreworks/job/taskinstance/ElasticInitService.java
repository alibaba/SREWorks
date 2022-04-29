package com.alibaba.sreworks.job.taskinstance;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ElasticInitService extends AbstractElasticsearchConfiguration {
    @Autowired
    ElasticsearchRestClientProperties elasticsearchRestClientProperties;

    @Override
    @Bean(destroyMethod = "close")
    public RestHighLevelClient elasticsearchClient() {
        String uri = elasticsearchRestClientProperties.getUris().get(0);
        uri = uri.replace("http://", "").replace("https://", "");
        log.info("elasticsearchRestClientProperties.getUris(): {}", uri);
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
            .connectedTo(uri)
            .withBasicAuth(
                elasticsearchRestClientProperties.getUsername(),
                elasticsearchRestClientProperties.getPassword()
            )
            .build();
        return RestClients.create(clientConfiguration).rest();
    }

}
