package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.Submission;
import com.javaevaluation.mapper.SubmissionMapper;
import com.javaevaluation.security.JwtUtils;
import com.javaevaluation.service.EvaluationService;
import com.javaevaluation.service.HomeworkOwnershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评测触发控制器(教师手动控制评测时机)
 */
@RestController
@RequestMapping("/api/teacher/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final SubmissionMapper submissionMapper;
    private final JwtUtils jwtUtils;
    private final HomeworkOwnershipService homeworkOwnershipService;

    /**
     * 触发单条提交评测
     */
    @PostMapping("/trigger/{submissionId}")
    public Result<Void> trigger(@PathVariable Integer submissionId,
                                @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = jwtUtils.getUserIdFromHeader(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Submission submission = submissionMapper.findById(submissionId);
        if (submission == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        if (!homeworkOwnershipService.isHomeworkOwnedBy(submission.getHomeworkId(), teacherId)) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        boolean ok = evaluationService.triggerOne(submissionId);
        if (!ok) {
            return Result.fail(ErrorCode.BAD_REQUEST, "当前状态不允许评测(评测中或已完成)");
        }
        return Result.success(null);
    }

    /**
     * 批量触发某作业下所有待评测/失败的提交
     * 请求体: {"homeworkId": 12}
     */
    @PostMapping("/trigger-batch")
    public Result<Map<String, Integer>> triggerBatch(@RequestBody Map<String, Integer> body,
                                                     @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = jwtUtils.getUserIdFromHeader(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Integer homeworkId = body == null ? null : body.get("homeworkId");
        if (homeworkId == null) {
            return Result.fail(ErrorCode.BAD_REQUEST, "homeworkId 不能为空");
        }
        if (!homeworkOwnershipService.isHomeworkOwnedBy(homeworkId, teacherId)) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        Map<String, Integer> stats = evaluationService.triggerBatch(homeworkId);
        return Result.success(stats);
    }
}
