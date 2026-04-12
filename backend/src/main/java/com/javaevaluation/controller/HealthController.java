package com.javaevaluation.controller;

import com.javaevaluation.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * 健康检查
     *
     * GET /api/v1/health
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "code-review-backend");
        healthInfo.put("timestamp", LocalDateTime.now());

        return Result.success(healthInfo);
    }

    /**
     * 根路径
     *
     * GET /api/v1
     */
    @GetMapping
    public Result<Map<String, String>> index() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "Code Review Backend API");
        info.put("version", "1.0.0");
        info.put("docs", "/api/v1/health");

        return Result.success(info);
    }
}