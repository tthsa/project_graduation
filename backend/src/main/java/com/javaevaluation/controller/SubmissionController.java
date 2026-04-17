package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.EvaluationResult;
import com.javaevaluation.service.CodeSubmitService;
import com.javaevaluation.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submission")
public class SubmissionController {

    @Autowired
    private CodeSubmitService codeSubmitService;

    @Autowired
    private ResultService resultService;

    /**
     * 提交作业
     */
    @PostMapping("/submit")
    public Result<Integer> submitHomework(
            @RequestParam Integer studentId,
            @RequestParam Integer homeworkId,
            @RequestBody List<String> filePaths) {
        Integer submissionId = codeSubmitService.submitHomework(studentId, homeworkId, filePaths);
        return Result.success(submissionId);
    }

    /**
     * 获取评测结果
     */
    @GetMapping("/result/{submissionId}")
    public Result<EvaluationResult> getResult(@PathVariable Integer submissionId) {
        EvaluationResult result = resultService.getResult(submissionId);
        if (result == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        return Result.success(result);
    }

    /**
     * 获取评测状态
     */
    @GetMapping("/status/{submissionId}")
    public Result<Integer> getStatus(@PathVariable Integer submissionId) {
        Integer status = codeSubmitService.getStatus(submissionId);
        if (status == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        return Result.success(status);
    }
}