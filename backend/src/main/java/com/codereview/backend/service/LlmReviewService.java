package com.codereview.backend.service;

import com.codereview.backend.dto.ExecutionResult;
import com.codereview.backend.properties.SiliconFlowProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
@Slf4j
@Service
@RequiredArgsConstructor

public class LlmReviewService {
    private final SiliconFlowProperties siliconFlowProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public String reviewCode(String code) {
        ExecutionResult result = new ExecutionResult();
        result.setSuccess(true);
        return reviewCode(code, result);
    }

    /**
     * 使用 LLM 评审 Java 代码（完整版）
     *
     * @param code Java 源代码
     * @param executionResult 执行结果
     * @return 评审意见
     */

    public String reviewCode(String code, ExecutionResult executionResult) {
        try {
            // 1. 构建提示词
            String prompt = buildPrompt(code, executionResult);

            // 2. 调用 SiliconFlow API
            String review = callSiliconFlowAPI(prompt);

            log.info("✅ LLM 评审完成: taskId={}", executionResult.getTaskId());
            return review;

        } catch (Exception e) {
            log.error("❌ LLM 评审失败: {}", e.getMessage());
            return "评审失败: " + e.getMessage();
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String code, ExecutionResult result) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的 Java 代码评审专家。请评审以下代码：\n\n");
        prompt.append("```java\n");
        prompt.append(code);
        prompt.append("\n```\n\n");

        if (result.getSuccess()) {
            prompt.append("执行结果：成功\n");
            if (result.getOutput() != null && !result.getOutput().isEmpty()) {
                prompt.append("输出：\n```\n");
                prompt.append(result.getOutput());
                prompt.append("\n```\n\n");
            }
        } else {
            prompt.append("执行结果：失败\n");
            if (result.getError() != null) {
                prompt.append("错误信息：\n```\n");
                prompt.append(result.getError());
                prompt.append("\n```\n\n");
            }
        }

        prompt.append("请从以下几个方面进行评审：\n");
        prompt.append("1. 代码质量和可读性\n");
        prompt.append("2. 潜在的 Bug 或问题\n");
        prompt.append("3. 性能优化建议\n");
        prompt.append("4. 最佳实践建议\n");
        prompt.append("5. 安全性问题\n\n");
        prompt.append("请用中文回答，简洁明了。");

        return prompt.toString();
    }

    /**
     * 调用 SiliconFlow API
     */
    private String callSiliconFlowAPI(String prompt) throws Exception {
        // 构建请求体
        String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":%s}],\"max_tokens\":2000}",
                siliconFlowProperties.getModel(),
                objectMapper.writeValueAsString(prompt)
        );

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(siliconFlowProperties.getApiKey());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // 发送请求
        String url = siliconFlowProperties.getBaseUrl() + "/v1/chat/completions";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        // 解析响应
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }
        }

        throw new RuntimeException("API 调用失败: " + response.getStatusCode());
    }

}