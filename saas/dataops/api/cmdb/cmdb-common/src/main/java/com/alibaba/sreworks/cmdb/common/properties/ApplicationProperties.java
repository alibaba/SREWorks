package com.alibaba.sreworks.cmdb.common.properties;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Data
public class ApplicationProperties {

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.url}")
    private String datasetUrl;

    @Value("${spring.datasource.username}")
    private String datasetUsername;

    @Value("${spring.datasource.password}")
    private String datasetPassword;

    @Value("${spring.elasticsearch.protocol}")
    private String esProtocol;

    @Value("${spring.elasticsearch.host}")
    private String esHost;

    @Value("${spring.elasticsearch.port}")
    private Integer esPort;
}