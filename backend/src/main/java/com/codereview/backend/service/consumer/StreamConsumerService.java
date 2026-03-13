package com.codereview.backend.service.consumer;

import com.codereview.backend.dto.CodeTask;
import com.codereview.backend.properties.ReviewProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

/**
 * Redis Stream 消费者服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamConsumerService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TaskProcessor taskProcessor;
    private final ReviewProperties reviewProperties;

    private String consumerGroup;
    private String consumerName;
    private String streamKey;

    @PostConstruct
    public void init() {
        this.streamKey = reviewProperties.getStream().getKey();
        this.consumerGroup = reviewProperties.getStream().getConsumerGroup();
        this.consumerName = "consumer-" + Thread.currentThread().getId();

        // 创建消费者组（如果不存在）
        createConsumerGroup();

        log.info("🚀 Stream 消费者初始化完成: stream={}, group={}, consumer={}",
                streamKey, consumerGroup, consumerName);
    }

    /**
     * 创建消费者组
     */
    private void createConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
            log.info("✅ 创建消费者组: {}", consumerGroup);
        } catch (Exception e) {
            // 如果组已存在，忽略错误
            if (e.getMessage().contains("BUSYGROUP")) {
                log.info("ℹ️ 消费者组已存在: {}", consumerGroup);
            } else {
                log.error("❌ 创建消费者组失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 消费 Stream 消息（定时任务）
     */
    @Scheduled(fixedDelay = 1000) // 每 1 秒执行一次
    public void consumeMessages() {
        try {
            // 从 Stream 读取消息
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                    Consumer.from(consumerGroup, consumerName),
                    StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            );

            if (records == null || records.isEmpty()) {
                return;
            }

            // 处理每条消息
            for (MapRecord<String, Object, Object> record : records) {
                processRecord(record);
            }

        } catch (Exception e) {
            log.error("❌ 消费消息失败: {}", e.getMessage());
        }
    }

    /**
     * 处理单条消息
     */
    private void processRecord(MapRecord<String, Object, Object> record) {
        String messageId = record.getId().getValue();

        try {
            log.info("📨 收到消息: messageId={}", messageId);

            // 解析消息
            CodeTask task = parseTask(record);

            if (task != null) {
                // 处理任务
                taskProcessor.processTask(task);
            }

            // 确认消息（ACK）
            redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
            log.info("✅ 消息已确认: messageId={}", messageId);

        } catch (Exception e) {
            log.error("❌ 处理消息失败: messageId={}, error={}", messageId, e.getMessage());
        }
    }

    /**
     * 解析任务
     */
    private CodeTask parseTask(MapRecord<String, Object, Object> record) {
        try {
            Object taskId = record.getValue().get("taskId");
            Object code = record.getValue().get("code");
            Object className = record.getValue().get("className");
            Object timeLimit = record.getValue().get("timeLimit");
            Object memoryLimit = record.getValue().get("memoryLimit");
            Object timestamp = record.getValue().get("timestamp");

            return CodeTask.builder()
                    .taskId(taskId != null ? taskId.toString() : null)
                    .code(code != null ? code.toString() : null)
                    .className(className != null ? className.toString() : "Main")
                    .timeLimit(timeLimit != null ? Integer.parseInt(timeLimit.toString()) : 10)
                    .memoryLimit(memoryLimit != null ? Integer.parseInt(memoryLimit.toString()) : 256)
                    .timestamp(timestamp != null ? Long.parseLong(timestamp.toString()) : System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("❌ 解析任务失败: {}", e.getMessage());
            return null;
        }
    }
}