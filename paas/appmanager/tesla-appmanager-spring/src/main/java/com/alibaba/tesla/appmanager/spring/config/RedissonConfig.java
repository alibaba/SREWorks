package com.alibaba.tesla.appmanager.spring.config;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redission 配置
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host:}")
    private String redisHost;

    @Value("${spring.redis.port:0}")
    private Integer redisPort;

    @Value("${spring.redis.database:0}")
    private Integer redisDatabase;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.sentinel.master:}")
    private String redisSentinelMaster;

    @Value("${spring.redis.sentinel.nodes:}")
    private String redisSentinelNodes;

    @Value("${spring.redis.sentinel.password:}")
    private String redisSentinelPassword;

    /**
     * 生成全局 Redisson Client
     *
     * @return RedissonClient
     */
    @Bean
    RedissonClient redissonClient() {
        Config config = new Config();

        if (StringUtils.isNotEmpty(redisSentinelMaster) && StringUtils.isNotEmpty(redisSentinelNodes)) {
            config.useSentinelServers()
                    .setMasterName(redisSentinelMaster)
                    .addSentinelAddress(redisSentinelNodes.split(","))
                    .setDatabase(redisDatabase)
                    .setPassword(StringUtils.isNotEmpty(redisSentinelPassword) ? redisSentinelPassword : null);
        } else {
            if (StringUtils.isEmpty(redisHost) || redisPort <= 0) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "invalid redis host & port in standalone mode");
            }
            config.useSingleServer()
                    .setAddress(String.format("%s:%d", redisHost, redisPort))
                    .setDatabase(redisDatabase)
                    .setPassword(StringUtils.isNotEmpty(redisPassword) ? redisPassword : null);
        }
        return Redisson.create(config);
    }
}
