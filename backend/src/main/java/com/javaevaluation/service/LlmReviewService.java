package com.javaevaluation.service;

import com.javaevaluation.dto.DimensionScore;
import com.javaevaluation.dto.ExecutionResult;
import com.javaevaluation.dto.LlmDimension;
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

import java.util.ArrayList;
import java.util.Collections;
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
     * 默认单维度配置（代码质量 100%）。
     */
    public static final List<LlmDimension> DEFAULT_DIMENSIONS =
            Collections.singletonList(new LlmDimension("代码质量", 100));

    /**
     * 评审代码（默认单维度）
     */
    public String reviewCode(Integer submissionId, ExecutionResult result) {
        return reviewCode(submissionId, result, DEFAULT_DIMENSIONS, null, null);
    }

    /**
     * 评审代码，按指定维度列表生成 prompt。
     * 传入作业标题和描述，让 LLM 判断代码是否符合题意。
     */
    public String reviewCode(Integer submissionId, ExecutionResult result,
                             List<LlmDimension> dimensions,
                             String homeworkTitle, String homeworkDescription) {
        try {
            List<SubmissionFile> files = submissionFileMapper.findBySubmissionId(submissionId);

            if (files == null || files.isEmpty()) {
                return "未找到提交的代码文件";
            }

            StringBuilder codeContent = new StringBuilder();
            for (SubmissionFile file : files) {
                codeContent.append("=== 文件: ").append(file.getFileName()).append(" ===\n");
                codeContent.append(file.getFileContent()).append("\n\n");
            }

            List<LlmDimension> useDims = (dimensions == null || dimensions.isEmpty())
                    ? DEFAULT_DIMENSIONS : dimensions;
            String content = buildReviewContent(codeContent.toString(), result, useDims,
                    homeworkTitle, homeworkDescription);

            return callLlmApi(content);

        } catch (Exception e) {
            log.error("LLM评审失败: {}", e.getMessage(), e);
            return "代码评审失败: " + e.getMessage();
        }
    }

    /**
     * 构建评审内容（按维度列表生成评分要求）。
     * 加入作业描述，要求 LLM 先判断代码是否符合题意。
     */
    private String buildReviewContent(String codeContent, ExecutionResult result,
                                      List<LlmDimension> dimensions,
                                      String homeworkTitle, String homeworkDescription) {
        StringBuilder prompt = new StringBuilder();

        // 1. 作业要求（题意判断的依据）
        prompt.append("## 作业要求\n");
        prompt.append("标题: ").append(homeworkTitle != null ? homeworkTitle : "无").append("\n");
        if (homeworkDescription != null && !homeworkDescription.isEmpty()) {
            prompt.append("描述: ").append(homeworkDescription).append("\n");
        }
        prompt.append("\n");

        // 2. 提交的代码
        prompt.append("## 提交的代码\n");
        prompt.append("```\n");
        prompt.append(codeContent);
        prompt.append("```\n\n");

        // 3. 测试结果
        prompt.append("## 测试结果\n");
        prompt.append("- 编译状态: ").append(result.getCompileStatus()).append("\n");
        if (result.getTestTotal() != null && result.getTestTotal() > 0) {
            prompt.append("- 测试通过: ").append(result.getTestPassed()).append("/").append(result.getTestTotal()).append("\n");
            prompt.append("- 得分: ").append(result.getTestScore() == null ? "未参与评分" : result.getTestScore() + "分").append("\n");
        } else {
            prompt.append("- 测试通过: 无测试用例\n");
        }

        if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) {
            prompt.append("- 错误信息: ").append(result.getErrorMessage()).append("\n");
        }

        // 4. 维度列表
        StringBuilder dimNames = new StringBuilder();
        for (int i = 0; i < dimensions.size(); i++) {
            if (i > 0) dimNames.append("、");
            dimNames.append(dimensions.get(i).getName());
        }

        // 5. 评测规则（强调题意符合度）
        prompt.append("\n## 评测规则（非常重要）\n");
        prompt.append("1. 首先判断提交的代码是否实现了作业要求的功能，是否与题目相关。\n");
        prompt.append("2. 如果代码明显不符合题意（例如：题目要求的功能未实现，或提交了完全无关的代码如加法器代替质数判断器），则直接判定为【题意符合度】不符合。\n");
        prompt.append("3. 不符合题意的代码，所有评分维度均为 0 分，并在评审意见中明确说明原因。\n");
        prompt.append("4. 只有符合题意的代码，才进行后续的代码质量等维度评分。\n\n");

        // 6. 输出格式
        prompt.append("## 请严格按以下格式给出评审意见\n");
        prompt.append("【题意符合度】符合/不符合\n");
        prompt.append("（如果判定为不符合，请在下方说明原因，然后直接结束，不需要给出维度评分。）\n\n");
        prompt.append("如果符合题意，请继续以下内容:\n");
        prompt.append("1. 各维度评分(每项满分10分,只写整数或一位小数):\n");
        for (LlmDimension d : dimensions) {
            prompt.append("   - ").append(d.getName()).append(": X分\n");
        }
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
            // 调试日志（注意：不要打印 API Key）
            log.info("=== LLM API 调试信息 ===");
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
     * 从评审结果中提取分数。匹配不到返回 null（区别于真实 0 分）。
     * 旧实现的两个坑：
     *   1) 半角冒号 `:` 不能匹配 LLM 输出的全角 `：`，且只接整数（8.5 漏掉）；
     *   2) 兜底的 `(\\d+)\\s*分` 会先吃到 prompt 里残留的 "满分10分" → 永远返回 10。
     */
    public Integer extractScore(String review) {
        if (review == null || review.isEmpty()) {
            return null;
        }

        try {
            // 主格式: "代码质量评价（满分10分）：8分" / "代码质量评价: 8.5分"
            Pattern pattern1 = Pattern.compile("代码质量评价[^：:]*[：:]\\s*(\\d+(?:\\.\\d+)?)\\s*分");
            Matcher matcher1 = pattern1.matcher(review);
            if (matcher1.find()) {
                return clampScore(matcher1.group(1));
            }

            // 备用格式: "8/10" / "8.5 / 10"
            Pattern pattern2 = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*/\\s*10(?!\\d)");
            Matcher matcher2 = pattern2.matcher(review);
            if (matcher2.find()) {
                return clampScore(matcher2.group(1));
            }

        } catch (Exception e) {
            log.warn("提取分数失败: {}", e.getMessage());
        }

        log.warn("未从 LLM 评审中提取到分数，evaluation_result.llm_score 将存 null");
        return null;
    }

    private Integer clampScore(String numStr) {
        double raw = Double.parseDouble(numStr);
        long rounded = Math.round(raw);
        if (rounded < 0) return 0;
        if (rounded > 10) return 10;
        return (int) rounded;
    }

    /**
     * 从 LLM 评审中提取题意符合度。
     * 返回 0=不符合（代码与题目无关），10=符合（代码实现了题目要求）。
     * 提取不到时默认返回 10（避免误判）。
     */
    public Integer extractComplianceScore(String review) {
        if (review == null || review.isEmpty()) {
            return 10;
        }
        try {
            // 主格式: 【题意符合度】符合 / 【题意符合度】不符合
            Pattern pattern = Pattern.compile("【题意符合度】\\s*(符合|不符合)");
            Matcher matcher = pattern.matcher(review);
            if (matcher.find()) {
                return "符合".equals(matcher.group(1)) ? 10 : 0;
            }
            // 回退：如果评审中有明确的不符合题意关键词
            String lower = review.toLowerCase();
            if (lower.contains("不符合题意") || lower.contains("与题目无关")
                    || lower.contains("明显偏离题目要求") || lower.contains("未实现题目要求")
                    || lower.contains("提交了完全无关的代码")) {
                return 0;
            }
        } catch (Exception e) {
            log.warn("提取题意符合度失败: {}", e.getMessage());
        }
        return 10; // 默认符合，避免误判
    }

    /**
     * 判断代码是否符合题意。
     */
    public boolean isCompliant(String review) {
        Integer score = extractComplianceScore(review);
        return score != null && score > 0;
    }

    /**
     * 按维度名从 LLM 评审中提取每维度得分 (0-10)。
     * 匹配格式: "维度名: X分" / "维度名: X.5分" / "维度名: X/10"，全角/半角冒号都支持。
     * 提取不到的维度记 null，调用方据此降级处理。
     */
    public List<DimensionScore> extractDimensionScores(String review, List<LlmDimension> dimensions) {
        List<DimensionScore> result = new ArrayList<>();
        if (review == null || review.isEmpty() || dimensions == null || dimensions.isEmpty()) {
            return result;
        }
        for (LlmDimension d : dimensions) {
            Integer score = extractOneDimension(review, d.getName());
            result.add(new DimensionScore(d.getName(), score));
        }
        return result;
    }

    private Integer extractOneDimension(String review, String dimName) {
        try {
            String safe = Pattern.quote(dimName);
            // "代码质量: 8分" / "代码质量：8.5分"，允许 - * 列表前缀
            Pattern p1 = Pattern.compile(safe + "\\s*[:：]\\s*(\\d+(?:\\.\\d+)?)\\s*分");
            Matcher m1 = p1.matcher(review);
            if (m1.find()) {
                return clampScore(m1.group(1));
            }
            // "代码质量: 8/10"
            Pattern p2 = Pattern.compile(safe + "\\s*[:：]\\s*(\\d+(?:\\.\\d+)?)\\s*/\\s*10(?!\\d)");
            Matcher m2 = p2.matcher(review);
            if (m2.find()) {
                return clampScore(m2.group(1));
            }
        } catch (Exception e) {
            log.warn("提取维度 [{}] 失败: {}", dimName, e.getMessage());
        }
        log.warn("未从评审中提取到维度 [{}] 的分数", dimName);
        return null;
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