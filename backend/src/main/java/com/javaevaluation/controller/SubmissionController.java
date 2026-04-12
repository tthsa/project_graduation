package com.javaevaluation.controller;

import com.javaevaluation.dto.ApiResponse;
import com.javaevaluation.dto.ExecutionResult;
import com.javaevaluation.service.CodeSubmitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业提交Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/submission")
@RequiredArgsConstructor
public class SubmissionController {  // 修改类名

    private final CodeSubmitService codeSubmitService;

    /**
     * 提交作业
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Integer>> submitHomework(
            @RequestParam Integer studentId,
            @RequestParam Integer homeworkId,
            @RequestBody List<String> filePaths) {

        Integer submissionId = codeSubmitService.submitHomework(studentId, homeworkId, filePaths);
        return ResponseEntity.ok(ApiResponse.success(submissionId));
    }

    /**
     * 获取评测结果
     */
    @GetMapping("/result/{submissionId}")
    public ResponseEntity<ApiResponse<ExecutionResult>> getResult(
            @PathVariable Integer submissionId) {

        ExecutionResult result = codeSubmitService.getResult(submissionId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取提交状态
     */
    @GetMapping("/status/{submissionId}")
    public ResponseEntity<ApiResponse<Integer>> getStatus(
            @PathVariable Integer submissionId) {

        Integer status = codeSubmitService.getStatus(submissionId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}