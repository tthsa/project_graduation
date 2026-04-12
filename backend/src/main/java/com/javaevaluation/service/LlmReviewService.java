package com.javaevaluation.service;

import com.javaevaluation.dto.ExecutionResult;
import com.javaevaluation.properties.SiliconFlowProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * LLM代码评审服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmReviewService {

    private final SiliconFlowProperties siliconFlowProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    /**
     * 评审代码（字符串版本）
     */
    public String reviewCode(String code) {
        try {
            String prompt = buildPrompt(code);
            return callSiliconFlowAPI(prompt);
        } catch (Exception e) {
            log.error("LLM评审失败: {}", e.getMessage());
            return "评审失败: " + e.getMessage();
        }
    }

    /**
     * 评审代码（ExecutionResult版本）
     */
    public String reviewCode(ExecutionResult result) {
        String content = buildReviewContent(result);
        return reviewCode(content);
    }

    /**
     * 从评审结果中提取分数
     */
    public Integer extractScore(String review) {
        try {
            if (review.contains("分数") || review.contains("得分")) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*分");
                java.util.regex.Matcher matcher = pattern.matcher(review);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }
        } catch (Exception e) {
            log.warn("提取分数失败: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * 构建评审内容
     */
    private String buildReviewContent(ExecutionResult result) {
        StringBuilder content = new StringBuilder();
        content.append("编译状态：").append(result.getCompileStatus()).append("\n");

        if (result.getTestPassed() != null && result.getTestTotal() != null) {
            content.append("测试通过：").append(result.getTestPassed())
                    .append("/").append(result.getTestTotal()).append("\n");
        }

        if (result.getOutput() != null) {
            content.append("输出：\n").append(result.getOutput()).append("\n");
        }

        if (result.getErrorMessage() != null) {
            content.append("错误：\n").append(result.getErrorMessage()).append("\n");
        }

        return content.toString();
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String code) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的Java代码评审专家。请评审以下代码：\n\n");
        prompt.append("```\n");
        prompt.append(code);
        prompt.append("\n```\n\n");
        prompt.append("请从以下几个方面进行评审：\n");
        prompt.append("1. 代码质量和可读性\n");
        prompt.append("2. 潜在的Bug或问题\n");
        prompt.append("3. 性能优化建议\n");
        prompt.append("4. 最佳实践建议\n");
        prompt.append("5. 安全性问题\n\n");
        prompt.append("请用中文回答，简洁明了。最后请给出一个0-100的分数，格式为：总分：XX分");
        return prompt.toString();
    }

    /**
     * 调用 SiliconFlow API
     */
    private String callSiliconFlowAPI(String prompt) throws Exception {
        String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":%s}],\"max_tokens\":2000}",
                siliconFlowProperties.getModel(),
                objectMapper.writeValueAsString(prompt)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(siliconFlowProperties.getApiKey());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String url = siliconFlowProperties.getBaseUrl() + "/v1/chat/completions";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }
        }

        throw new RuntimeException("API调用失败: " + response.getStatusCode());
    }
}