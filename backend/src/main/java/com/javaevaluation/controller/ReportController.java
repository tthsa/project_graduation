package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.Submission;
import com.javaevaluation.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报告控制器（教师查看学生提交和评测结果）
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final SubmissionMapper submissionMapper;

    /**
     * 查看某作业的所有学生提交情况（教师）
     */
    @GetMapping("/homework/{homeworkId}")
    public Result<List<Submission>> listByHomeworkId(@PathVariable Integer homeworkId) {
        List<Submission> submissions = submissionMapper.findByHomeworkId(homeworkId);
        return Result.success(submissions);
    }

    /**
     * 查看某学生的所有提交记录（教师）
     */
    @GetMapping("/student/{studentId}")
    public Result<List<Submission>> listByStudentId(@PathVariable Integer studentId) {
        List<Submission> submissions = submissionMapper.findByStudentId(studentId);
        return Result.success(submissions);
    }

    /**
     * 查看提交详情和评测结果（教师）
     */
    @GetMapping("/detail/{submissionId}")
    public Result<Submission> getDetail(@PathVariable Integer submissionId) {
        Submission submission = submissionMapper.findById(submissionId);
        if (submission == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        return Result.success(submission);
    }
}