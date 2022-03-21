package com.alibaba.tesla.appmanager.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 异步执行器配置
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@ConfigurationProperties(prefix = "appmanager.async-executor")
public class AsyncExecutorProperties {

    private Integer coreSize = 200;

    private Integer maxSize = 400;

    private Integer queueCapacity = 100;
}
