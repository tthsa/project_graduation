package com.javaevaluation.controller;

import com.javaevaluation.common.Result;
import com.javaevaluation.service.LlmReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
     * 提取分数
     */
    @PostMapping("/extract-score")
    public Result<Integer> extractScore(@RequestBody String review) {
        Integer score = llmReviewService.extractScore(review);
        return Result.success(score);
    }
}