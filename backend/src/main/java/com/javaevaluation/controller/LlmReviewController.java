package com.javaevaluation.controller;

import com.javaevaluation.dto.ApiResponse;
import com.javaevaluation.service.LlmReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * LLM代码评审Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmReviewController {

    private final LlmReviewService llmReviewService;

    /**
     * 评审代码
     */
    @PostMapping("/review")
    public ResponseEntity<ApiResponse<String>> reviewCode(@RequestBody String code) {
        String review = llmReviewService.reviewCode(code);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    /**
     * 提取分数
     */
    @PostMapping("/extract-score")
    public ResponseEntity<ApiResponse<Integer>> extractScore(@RequestBody String review) {
        Integer score = llmReviewService.extractScore(review);
        return ResponseEntity.ok(ApiResponse.success(score));
    }
}