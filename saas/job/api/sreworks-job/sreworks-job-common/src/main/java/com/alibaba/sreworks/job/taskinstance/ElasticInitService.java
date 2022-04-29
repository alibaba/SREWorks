package com.alibaba.sreworks.job.taskinstance;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ElasticInitService extends AbstractElasticsearchConfiguration {
    @Autowired
    ElasticsearchRestClientProperties elasticsearchRestClientProperties;
    //
    //@Bean
    //public RestHighLevelClient restHighLevelClient(@Autowired RestClientBuilder restClientBuilder) {
    //    return new RestHighLevelClient(
    //        restClientBuilder
    //            .setHttpClientConfigCallback(requestConfig -> requestConfig
    //                .setKeepAliveStrategy((response, context) -> TimeUnit.MINUTES.toMillis(3))
    //            )
    //    );
    //
    //}

    @Override
    @Bean(destroyMethod = "close")
    public RestHighLevelClient elasticsearchClient() {
        log.info("elasticsearchRestClientProperties.getUris(): {}", elasticsearchRestClientProperties.getUris());
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
            .connectedTo(elasticsearchRestClientProperties.getUris().get(0))
            .withBasicAuth(
                elasticsearchRestClientProperties.getUsername(),
                elasticsearchRestClientProperties.getPassword()
            )
            .build();
        return RestClients.create(clientConfiguration).rest();
    }

}
