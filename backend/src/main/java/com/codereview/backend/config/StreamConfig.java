package com.codereview.backend.config;

import com.codereview.backend.properties.ReviewProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
@Slf4j
@Configuration
@RequiredArgsConstructor

public class StreamConfig {

    private final RedisConnectionFactory connectionFactory;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReviewProperties reviewProperties;

    /**
     * 初始化 Stream 和消费者组
     */
    @PostConstruct
    public void initStream() {
        String streamKey = reviewProperties.getStream().getKey();
        String groupName = reviewProperties.getStream().getConsumerGroup();

        try {
            // 检查 Stream 是否存在，不存在则创建
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(streamKey))) {
                // 创建 Stream（添加一条初始消息）
                redisTemplate.opsForStream().add(streamKey, java.util.Map.of("init", "true"));
                log.info("✅ 创建 Stream: {}", streamKey);
            }

            // 创建消费者组（如果不存在）
            try {
                redisTemplate.opsForStream().createGroup(streamKey, groupName);
                log.info("✅ 创建消费者组: {}", groupName);
            } catch (Exception e) {
                if (e.getMessage().contains("BUSYGROUP")) {
                    log.info("ℹ️ 消费者组已存在: {}", groupName);
                } else {
                    throw e;
                }
            }

            // 初始化结果流
            String resultStreamKey = reviewProperties.getResult().getStream();
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(resultStreamKey))) {
                redisTemplate.opsForStream().add(resultStreamKey, java.util.Map.of("init", "true"));
                log.info("✅ 创建结果流: {}", resultStreamKey);
            }

        } catch (Exception e) {
            log.error("❌ 初始化 Stream 失败: {}", e.getMessage());
            throw new RuntimeException("初始化 Stream 失败", e);
        }
    }
}
