package com.alibaba.tesla.appmanager.common.service.impl;

import com.alibaba.tesla.appmanager.autoconfig.PackageProperties;
import com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant;
import com.alibaba.tesla.appmanager.common.service.StreamLogService;
import com.alibaba.tesla.appmanager.server.storage.Storage;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * stream log 助手
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
//@Component
@Slf4j
@Service
public class StreamLogServiceImpl implements StreamLogService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PackageProperties packageProperties;

    @Autowired
    private Storage storage;

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

    public void clean(String streamKey,boolean storeLog) {
        if(storeLog){
            storeLog(streamKey);
        }
        redisTemplate.delete(streamKey);
    }

    public void clean(String streamKey, String msg, boolean storeLog) {
        redisTemplate.opsForStream().add(streamKey,
                ImmutableMap.of(RedisKeyConstant.STREAM_LOG_KEY, msg));
        if(storeLog){
            storeLog(streamKey);
        }
        redisTemplate.delete(streamKey);
    }

    public void clean(String streamKey, String msg, Logger logger, boolean storeLog) {
        logger.info(msg);
        redisTemplate.opsForStream().add(streamKey,
                ImmutableMap.of(RedisKeyConstant.STREAM_LOG_KEY, msg));
        if(storeLog){
            storeLog(streamKey);
        }
        redisTemplate.delete(streamKey);
    }

    private void storeLog(String streamKey){
        String bucketName = packageProperties.getBucketName();
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String objectName = String.format("stream_log/%s/%s.txt", date, streamKey);
        List<ObjectRecord<String, String>> records = redisTemplate.opsForStream()
                .read(String.class, StreamOffset.fromStart(streamKey));
        StringBuilder sb = new StringBuilder();
        for(ObjectRecord<String, String> rec : records) {
            sb.append(rec.getValue());
        }
        storage.putObject(bucketName, objectName, new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

}
