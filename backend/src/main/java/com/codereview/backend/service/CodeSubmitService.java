package com.codereview.backend.service;

import com.codereview.backend.dto.CodeSubmitRequest;
import com.codereview.backend.dto.CodeTask;
import com.codereview.backend.properties.ReviewProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor

public class CodeSubmitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ReviewProperties reviewProperties;
    private final ObjectMapper objectMapper;

    /**
     * 提交代码任务
     *
     * @param request 提交请求
     * @return 任务 ID
     */
    public String submitCode(CodeSubmitRequest request) {
        // 1. 生成任务 ID
        String taskId = UUID.randomUUID().toString();

        // 2. 设置默认值
        String className = request.getClassName();
        if (className == null || className.isEmpty()) {
            className = "Main";
        }

        Integer timeLimit = request.getTimeLimit();
        if (timeLimit == null || timeLimit <= 0) {
            timeLimit = 10;
        }

        Integer memoryLimit = request.getMemoryLimit();
        if (memoryLimit == null || memoryLimit <= 0) {
            memoryLimit = 256;
        }

        // 3. 创建任务对象
        CodeTask task = CodeTask.builder()
                .taskId(taskId)
                .code(request.getCode())
                .className(className)
                .timeLimit(timeLimit)
                .memoryLimit(memoryLimit)
                .timestamp(System.currentTimeMillis())
                .build();

        // 4. 发送到 Redis Stream
        try {
            Map<String, String> message = new HashMap<>();
            message.put("taskId", task.getTaskId());
            message.put("code", task.getCode());
            message.put("className", task.getClassName());
            message.put("timeLimit", task.getTimeLimit().toString());
            message.put("memoryLimit", task.getMemoryLimit().toString());
            message.put("timestamp", task.getTimestamp().toString());

            String streamKey = reviewProperties.getStream().getKey();
            redisTemplate.opsForStream().add(streamKey, message);

            log.info("✅ 任务已提交: taskId={}, className={}", taskId, className);

        } catch (Exception e) {
            log.error("❌ 提交任务失败: {}", e.getMessage());
            throw new RuntimeException("提交任务失败", e);
        }

        return taskId;
    }
}
