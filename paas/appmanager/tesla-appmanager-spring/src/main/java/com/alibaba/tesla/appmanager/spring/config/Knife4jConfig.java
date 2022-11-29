package com.alibaba.tesla.appmanager.spring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public GroupedOpenApi userApi(){
        String[] paths = { "/**" };
        String[] packagedToMatch = { "com.alibaba.tesla.appmanager" };
        return GroupedOpenApi.builder()
                .group("AppManager")
                .pathsToMatch(paths)
                .packagesToScan(packagedToMatch).build();
    }
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AppManager API")
                        .version("1.0")
                        .description("AppManager API")
                        .termsOfService("https://github.com/alibaba/SREWorks"));
    }
}