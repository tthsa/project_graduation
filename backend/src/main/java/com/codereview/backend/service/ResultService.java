package com.codereview.backend.service;

import com.codereview.backend.dto.ExecutionResult;
import com.codereview.backend.properties.ReviewProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
@RequiredArgsConstructor

public class ResultService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ReviewProperties reviewProperties;
    private final ObjectMapper objectMapper;

    /**
     * 保存执行结果
     *
     * @param result 执行结果
     */
    public void saveResult(ExecutionResult result) {
        try {
            String resultKey = "result:" + result.getTaskId();

            // 保存到 Redis Hash
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("taskId", result.getTaskId());
            resultMap.put("success", result.getSuccess().toString());
            resultMap.put("output", result.getOutput() != null ? result.getOutput() : "");
            resultMap.put("error", result.getError() != null ? result.getError() : "");
            resultMap.put("exitCode", result.getExitCode() != null ? result.getExitCode().toString() : "-1");
            resultMap.put("executionTime", result.getExecutionTime() != null ? result.getExecutionTime().toString() : "0");
            resultMap.put("llmReview", result.getLlmReview() != null ? result.getLlmReview() : "");
            resultMap.put("timestamp", result.getTimestamp().toString());

            redisTemplate.opsForHash().putAll(resultKey, resultMap);

            // 设置 TTL
            redisTemplate.expire(resultKey, reviewProperties.getResult().getTtl(), TimeUnit.SECONDS);

            log.info("💾 保存结果: taskId={}", result.getTaskId());

        } catch (Exception e) {
            log.error("❌ 保存结果失败: {}", e.getMessage());
        }
    }

    /**
     * 查询执行结果
     *
     * @param taskId 任务 ID
     * @return 执行结果（如果不存在返回 null）
     */
    public ExecutionResult getResult(String taskId) {
        try {
            String resultKey = "result:" + taskId;
            Map<Object, Object> resultMap = redisTemplate.opsForHash().entries(resultKey);

            if (resultMap.isEmpty()) {
                return null;
            }

            return ExecutionResult.builder()
                    .taskId(taskId)
                    .success(Boolean.parseBoolean(resultMap.get("success").toString()))
                    .output(resultMap.get("output").toString())
                    .error(resultMap.get("error").toString())
                    .exitCode(Integer.parseInt(resultMap.get("exitCode").toString()))
                    .executionTime(Long.parseLong(resultMap.get("executionTime").toString()))
                    .llmReview(resultMap.get("llmReview").toString())
                    .timestamp(Long.parseLong(resultMap.get("timestamp").toString()))
                    .build();

        } catch (Exception e) {
            log.error("❌ 查询结果失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查任务是否存在
     *
     * @param taskId 任务 ID
     * @return 是否存在
     */
    public boolean exists(String taskId) {
        String resultKey = "result:" + taskId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(resultKey));
    }

}
