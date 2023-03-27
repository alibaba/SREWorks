package com.alibaba.tesla.appmanager.common.util;

import com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * stream log 助手
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Component
@Slf4j
public class StreamLogHelper {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void info(String streamKey, String msg) {
        redisTemplate.opsForStream().add(streamKey,
                ImmutableMap.of(RedisKeyConstant.STREAM_LOG_KEY, msg));
        redisTemplate.expire(streamKey,10, TimeUnit.DAYS);
    }

    public void info(String streamKey, String msg, Logger logger) {
        logger.info(msg);
        redisTemplate.opsForStream().add(streamKey,
                ImmutableMap.of(RedisKeyConstant.STREAM_LOG_KEY, msg));
        redisTemplate.expire(streamKey,10, TimeUnit.DAYS);
    }

    public void clean(String streamKey) {
        redisTemplate.delete(streamKey);
    }

    public void clean(String streamKey, String msg) {
        redisTemplate.opsForStream().add(streamKey,
                ImmutableMap.of(RedisKeyConstant.STREAM_LOG_KEY, msg));
        redisTemplate.delete(streamKey);
    }

    public void clean(String streamKey, String msg, Logger logger) {
        logger.info(msg);
        redisTemplate.opsForStream().add(streamKey,
                ImmutableMap.of(RedisKeyConstant.STREAM_LOG_KEY, msg));
        redisTemplate.delete(streamKey);
    }

}
