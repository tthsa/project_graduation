package com.codereview.backend.service;

import com.codereview.backend.properties.SiliconFlowProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor

public class LlmReviewService {
    private final SiliconFlowProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /** 代码评审主入口*/
    public String reviewCode(String code, String language) {
        log.info("开始评审代码，语言: {}, 代码长度: {}", language, code.length());
        String prompt = buildReviewPrompt(code, language);
        return chat(prompt);
    }

    /**多轮对话：追问功能*/
    public String askQuestion(String submissionId, String question, String previousResult) {
        log.info("收到追问，submissionId: {}", submissionId);
        String prompt = buildFollowUpPrompt(submissionId, question, previousResult);
        return chat(prompt);
    }

    /**核心 Chat 方法*/
    private String chat(String userMessage) {
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new Message("system", getSystemPrompt()));
            messages.add(new Message("user", userMessage));
            ChatRequest chatRequest = new ChatRequest(
                    properties.getModel(),
                    messages,
                    0.7
            );
            String requestBody = objectMapper.writeValueAsString(chatRequest);
            log.info("请求模型: {}", properties.getModel());
            Request request = new Request.Builder()
                    .url(properties.getBaseUrl() + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + properties.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "无错误详情";
                    log.error("API 调用失败: {} - {}", response.code(), errorBody);
                    throw new RuntimeException("LLM 调用失败: " + response.code());
                }
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
                log.info("调用成功，返回内容长度: {}", content.length());
                return content;
            }
        } catch (Exception e) {
            log.error("调用异常", e);
            throw new RuntimeException("LLM 服务异常: " + e.getMessage(), e);
        }
    }

    private String getSystemPrompt() {
        return "你是一位资深的代码评审专家，拥有10年以上的软件开发经验。\n" +
                "你的任务是帮助学生改进代码质量，提供专业、友好、易懂的建议。\n\n" +
                "评审原则：\n" +
                "1. 先肯定代码的优点，再指出问题\n" +
                "2. 问题按严重程度排序：错误 > 警告 > 建议\n" +
                "3. 每个问题都要给出具体的修改建议和示例代码\n" +
                "4. 最后给出一个 0-100 的综合评分\n\n" +
                "输出格式：\n" +
                "### 代码优点\n- ...\n\n" +
                "### 发现的问题\n#### 错误（必须修复）\n...\n\n" +
                "### 综合评分\n**XX/100**\n\n" +
                "### 总结\n一句话总结...";
    }
    private String buildReviewPrompt(String code, String language) {
        return "请评审以下 " + language + " 代码：\n\n" +
                "```" + language + "\n" + code + "\n```\n\n" +
                "请按照系统提示中的格式进行评审。";
    }
    private String buildFollowUpPrompt(String submissionId, String question, String previousResult) {
        return "之前的评审结果：\n" + previousResult + "\n\n" +
                "学生的追问：\n" + question + "\n\n" +
                "请针对学生的问题进行详细解答。";
    }

    @Data
    @AllArgsConstructor
    private static class ChatRequest {
        private String model;
        private List<Message> messages;
        private double temperature;
    }
    @Data
    @AllArgsConstructor
    private static class Message {
        private String role;
        private String content;
    }

}