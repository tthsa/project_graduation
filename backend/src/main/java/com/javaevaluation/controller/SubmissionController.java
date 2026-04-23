package com.javaevaluation.controller;

import com.javaevaluation.entity.EvaluationResult;
import com.javaevaluation.entity.SubmissionFile;
import com.javaevaluation.service.CodeSubmitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final CodeSubmitService codeSubmitService;

    /**
     * 提交作业
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitHomework(
            @RequestParam(name = "studentId") Integer studentId,
            @RequestParam(name = "homeworkId") Integer homeworkId,
            @RequestParam(name = "files") MultipartFile[] files) {
        try {
            // 验证文件
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "请上传文件"
                ));
            }

            // 验证文件类型
            for (MultipartFile file : files) {
                String filename = file.getOriginalFilename();
                if (filename == null || !filename.endsWith(".java")) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "code", 400,
                            "message", "只支持.java文件"
                    ));
                }
            }

            Integer submissionId = codeSubmitService.submitHomework(studentId, homeworkId, files);
            return ResponseEntity.ok().body(Map.of(
                    "code", 200,
                    "message", "提交成功",
                    "data", Map.of("submissionId", submissionId)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "提交失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取提交的文件列表
     */
    @GetMapping("/files/{submissionId}")
    public ResponseEntity<?> getSubmissionFiles(@PathVariable(name = "submissionId") Integer submissionId) {
        List<SubmissionFile> files = codeSubmitService.getSubmissionFiles(submissionId);
        return ResponseEntity.ok().body(Map.of(
                "code", 200,
                "data", files
        ));
    }

    /**
     * 获取评测结果
     */
    @GetMapping("/result/{submissionId}")
    public ResponseEntity<?> getResult(@PathVariable(name = "submissionId") Integer submissionId) {
        EvaluationResult result = codeSubmitService.getResult(submissionId);
        return ResponseEntity.ok().body(Map.of(
                "code", 200,
                "data", result
        ));
    }

    /**
     * 获取提交状态
     */
    @GetMapping("/status/{submissionId}")
    public ResponseEntity<?> getStatus(@PathVariable(name = "submissionId") Integer submissionId) {
        Integer status = codeSubmitService.getStatus(submissionId);
        return ResponseEntity.ok().body(Map.of(
                "code", 200,
                "data", Map.of("status", status)
        ));
    }
}