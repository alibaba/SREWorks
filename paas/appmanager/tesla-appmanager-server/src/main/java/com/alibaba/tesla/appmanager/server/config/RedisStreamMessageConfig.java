package com.alibaba.tesla.appmanager.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RedisStreamMessageConfig {

    @Autowired
    private LettuceConnectionFactory redisConnectionFactory;

    /**
     * StreamMessageListenerContainer
     * 1. 可以动态添加和删除消费者
     * 2. 可以进行消费组消费
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer() {
        AtomicInteger index = new AtomicInteger(1);
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(processors, processors, 0, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("async-stream-consumer-" + index.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        // 一次最多获取多少条消息
                        .batchSize(10)
                        // 运行 Stream 的 poll task
                        .executor(executor)
                        // Stream 中没有消息时，阻塞多长时间，需要比 `spring.redis.timeout` 的时间小
                        .pollTimeout(Duration.ofSeconds(1))
                        // 获取消息的过程或获取到消息给具体的消息者处理的过程中，发生了异常的处理
//                        .errorHandler(new CustomErrorHandler())
                        .build();
        return StreamMessageListenerContainer.create(redisConnectionFactory, options);
    }
}
