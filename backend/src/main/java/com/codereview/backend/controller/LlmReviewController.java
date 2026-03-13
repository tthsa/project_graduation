package com.codereview.backend.controller;

import com.codereview.backend.service.LlmReviewService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor

public class LlmReviewController {

    private final LlmReviewService llmReviewService;
    @PostMapping("/llm")
    public String testLlm(@RequestBody TestRequest request) {
        return llmReviewService.reviewCode(request.getCode(), request.getLanguage());
    }

}



@Data
class TestRequest {
    private String language;
    private String code;

}
