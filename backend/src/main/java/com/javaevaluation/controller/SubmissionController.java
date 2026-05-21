package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.EvaluationResult;
import com.javaevaluation.entity.Submission;
import com.javaevaluation.entity.SubmissionFile;
import com.javaevaluation.mapper.SubmissionMapper;
import com.javaevaluation.utils.JwtUtils;
import com.javaevaluation.service.CodeSubmitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final CodeSubmitService codeSubmitService;
    private final SubmissionMapper submissionMapper;
    private final JwtUtils jwtUtils;

    /**
     * 提交作业
     */
    @PostMapping("/submit")
    public Result<Map<String, Integer>> submitHomework(
            @RequestParam(name = "homeworkId") Integer homeworkId,
            @RequestParam(name = "files") MultipartFile[] files,
            @RequestHeader("Authorization") String authHeader) {
        Integer studentId = jwtUtils.getUserIdFromHeader(authHeader);
        if (studentId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        if (files == null || files.length == 0) {
            return Result.fail(ErrorCode.BAD_REQUEST, "请上传文件");
        }

        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            if (filename == null || filename.isBlank()) {
                return Result.fail(ErrorCode.BAD_REQUEST, "文件名不能为空");
            }
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return Result.fail(ErrorCode.BAD_REQUEST, "文件名非法");
            }
            if (!filename.endsWith(".java")) {
                return Result.fail(ErrorCode.BAD_REQUEST, "只支持 .java 文件");
            }
        }

        try {
            Integer submissionId = codeSubmitService.submitHomework(studentId, homeworkId, files);
            Map<String, Integer> data = new HashMap<>();
            data.put("submissionId", submissionId);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail(ErrorCode.SUBMIT_FAILED, "提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前学生的所有提交记录
     */
    @GetMapping("/list")
    public Result<List<Submission>> listMySubmissions(
            @RequestHeader("Authorization") String authHeader) {
        Integer studentId = jwtUtils.getUserIdFromHeader(authHeader);
        if (studentId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        return Result.success(submissionMapper.findByStudentId(studentId));
    }

    /**
     * 获取提交详情（仅本人可见）
     */
    @GetMapping("/detail/{submissionId}")
    public Result<Submission> getMySubmissionDetail(
            @PathVariable Integer submissionId,
            @RequestHeader("Authorization") String authHeader) {
        Integer studentId = jwtUtils.getUserIdFromHeader(authHeader);
        if (studentId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        if (!studentId.equals(submission.getStudentId())) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        return Result.success(submission);
    }

    /**
     * 获取提交的文件列表
     * 学生只能看自己的;教师/管理员可看任意
     */
    @GetMapping("/files/{submissionId}")
    public Result<List<SubmissionFile>> getSubmissionFiles(
            @PathVariable(name = "submissionId") Integer submissionId,
            @RequestHeader("Authorization") String authHeader) {
        if (!canAccessSubmission(submissionId, authHeader)) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        List<SubmissionFile> files = codeSubmitService.getSubmissionFiles(submissionId);
        return Result.success(files);
    }

    /**
     * 获取评测结果
     * 学生只能看自己的;教师/管理员可看任意
     */
    @GetMapping("/result/{submissionId}")
    public Result<EvaluationResult> getResult(
            @PathVariable(name = "submissionId") Integer submissionId,
            @RequestHeader("Authorization") String authHeader) {
        if (!canAccessSubmission(submissionId, authHeader)) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        EvaluationResult result = codeSubmitService.getResult(submissionId);
        return Result.success(result);
    }

    /**
     * 获取提交状态
     */
    @GetMapping("/status/{submissionId}")
    public Result<Map<String, Integer>> getStatus(@PathVariable(name = "submissionId") Integer submissionId) {
        Integer status = codeSubmitService.getStatus(submissionId);
        Map<String, Integer> data = new HashMap<>();
        data.put("status", status);
        return Result.success(data);
    }

    private boolean canAccessSubmission(Integer submissionId, String authHeader) {
        String userType = jwtUtils.getUserTypeFromHeader(authHeader);
        if (userType == null) {
            return false;
        }
        if ("teacher".equals(userType) || "admin".equals(userType)) {
            return true;
        }
        Integer userId = jwtUtils.getUserIdFromHeader(authHeader);
        if (userId == null) {
            return false;
        }
        Submission submission = submissionMapper.selectById(submissionId);
        return submission != null && userId.equals(submission.getStudentId());
    }
}
