package com.javaevaluation.service;

import com.javaevaluation.dto.ExecutionResult;
import com.javaevaluation.entity.SubmissionFile;
import com.javaevaluation.mapper.SubmissionFileMapper;
import com.javaevaluation.properties.SiliconFlowProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM代码评审服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmReviewService {

    private final RestTemplate restTemplate;
    private final SubmissionFileMapper submissionFileMapper;
    private final SiliconFlowProperties siliconFlowProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 评审代码
     * @param submissionId 提交ID
     * @param result 执行结果
     * @return 评审结果
     */
    public String reviewCode(Integer submissionId, ExecutionResult result) {
        try {
            // 1. 从数据库读取代码内容
            List<SubmissionFile> files = submissionFileMapper.findBySubmissionId(submissionId);

            if (files == null || files.isEmpty()) {
                return "未找到提交的代码文件";
            }

            // 2. 构建代码内容字符串
            StringBuilder codeContent = new StringBuilder();
            for (SubmissionFile file : files) {
                codeContent.append("=== 文件: ").append(file.getFileName()).append(" ===\n");
                codeContent.append(file.getFileContent()).append("\n\n");
            }

            // 3. 构建评审内容
            String content = buildReviewContent(codeContent.toString(), result);

            // 4. 调用LLM评审
            return callLlmApi(content);

        } catch (Exception e) {
            log.error("LLM评审失败: {}", e.getMessage(), e);
            return "代码评审失败: " + e.getMessage();
        }
    }

    /**
     * 构建评审内容
     */
    private String buildReviewContent(String codeContent, ExecutionResult result) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("请对以下Java代码进行评审，从代码质量、逻辑正确性、可读性等方面给出评价和建议。\n\n");

        prompt.append("## 提交的代码\n");
        prompt.append("```\n");
        prompt.append(codeContent);
        prompt.append("```\n\n");

        prompt.append("## 测试结果\n");
        prompt.append("- 编译状态: ").append(result.getCompileStatus()).append("\n");
        prompt.append("- 测试通过: ").append(result.getTestPassed()).append("/").append(result.getTestTotal()).append("\n");
        prompt.append("- 得分: ").append(result.getTestScore()).append("分\n");

        if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) {
            prompt.append("- 错误信息: ").append(result.getErrorMessage()).append("\n");
        }

        prompt.append("\n## 请按以下格式给出评审意见\n");
        prompt.append("1. 代码质量评价（满分10分）: X分\n");
        prompt.append("2. 代码优点\n");
        prompt.append("3. 代码问题和改进建议\n");
        prompt.append("4. 总体评价\n");

        return prompt.toString();
    }

    /**
     * 调用LLM API
     */
    private String callLlmApi(String content) {
        try {
            // 调试日志
            log.info("=== LLM API 调试信息 ===");
            log.info("API Key: {}", siliconFlowProperties.getApiKey());
            log.info("Base URL: {}", siliconFlowProperties.getBaseUrl());
            log.info("Model: {}", siliconFlowProperties.getModel());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + siliconFlowProperties.getApiKey());

            // 构建请求体
            String requestBody = String.format(
                    "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                    siliconFlowProperties.getModel(),
                    escapeJson(content)
            );

            log.info("Request Body: {}", requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            String apiUrl = siliconFlowProperties.getBaseUrl() + "/v1/chat/completions";
            log.info("API URL: {}", apiUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseLlmResponse(response.getBody());
            }

            return "LLM调用失败: " + response.getStatusCode();

        } catch (Exception e) {
            log.error("调用LLM API失败: {}", e.getMessage(), e);
            return "LLM调用异常: " + e.getMessage();
        }
    }

    /**
     * 解析LLM响应
     */
    private String parseLlmResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // 适配OpenAI格式（硅基流动兼容）
            if (root.has("choices") && root.get("choices").isArray()) {
                JsonNode choices = root.get("choices");
                if (choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }

            // 适配其他格式
            if (root.has("output")) {
                return root.get("output").asText();
            }

            if (root.has("result")) {
                return root.get("result").asText();
            }

            return responseBody;
        } catch (Exception e) {
            log.warn("解析LLM响应失败: {}", e.getMessage());
            return responseBody;
        }
    }

    /**
     * 从评审结果中提取分数
     */
    public Integer extractScore(String review) {
        if (review == null || review.isEmpty()) {
            return 0;
        }

        try {
            // 尝试匹配 "代码质量评价（满分10分）: 8分" 格式
            Pattern pattern1 = Pattern.compile("代码质量评价[^:]*:\\s*(\\d+)\\s*分");
            Matcher matcher1 = pattern1.matcher(review);
            if (matcher1.find()) {
                return Integer.parseInt(matcher1.group(1));
            }

            // 尝试匹配 "X分" 格式（第一个数字）
            Pattern pattern2 = Pattern.compile("(\\d+)\\s*分");
            Matcher matcher2 = pattern2.matcher(review);
            if (matcher2.find()) {
                return Integer.parseInt(matcher2.group(1));
            }

            // 尝试匹配 "X/10" 格式
            Pattern pattern3 = Pattern.compile("(\\d+)\\s*/\\s*10");
            Matcher matcher3 = pattern3.matcher(review);
            if (matcher3.find()) {
                return Integer.parseInt(matcher3.group(1));
            }

        } catch (Exception e) {
            log.warn("提取分数失败: {}", e.getMessage());
        }

        return 0;
    }

    /**
     * 转义JSON字符串
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}