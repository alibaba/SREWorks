package com.alibaba.sreworks.job.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@Data
public class ElasticsearchRestClientProperties {
    @Value("${spring.elasticsearch.rest.keep-alive-timeout}")
    private long keepAliveTimeout;

    @Value("${spring.elasticsearch.rest.uris}")
    private List<String> uris = new ArrayList<>(Collections.singletonList("http://localhost:9200"));

    @Value("${spring.elasticsearch.rest.username}")
    private String username;

    @Value("${spring.elasticsearch.rest.password}")
    private String password;

    private Duration connectionTimeout = Duration.ofSeconds(1L);

    private Duration readTimeout = Duration.ofSeconds(30L);
}