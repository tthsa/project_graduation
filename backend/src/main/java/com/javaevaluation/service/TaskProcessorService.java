package com.javaevaluation.service;

import com.javaevaluation.dto.CodeTask;
import com.javaevaluation.dto.ExecutionResult;
import com.javaevaluation.entity.EvaluationResult;
import com.javaevaluation.entity.Submission;
import com.javaevaluation.mapper.EvaluationResultMapper;
import com.javaevaluation.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 任务处理服务
 * 处理评测任务的核心逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskProcessorService {

    private final SubmissionMapper submissionMapper;
    private final EvaluationResultMapper evaluationResultMapper;
    private final DockerSandboxService dockerSandboxService;
    private final LlmReviewService llmReviewService;

    /**
     * 处理评测任务
     */
    @Transactional
    public void processTask(CodeTask task) {
        log.info("开始处理评测任务: taskId={}, submissionId={}", task.getTaskId(), task.getSubmissionId());

        try {
            // 1. 更新提交状态为"评测中"
            updateSubmissionStatus(task.getSubmissionId(), 1);

            // 2. 在Docker中执行评测
            ExecutionResult result = dockerSandboxService.executeTask(task);

            // 3. LLM代码评审
            String llmReview = null;
            Integer llmScore = 0;
            if ("SUCCESS".equals(result.getCompileStatus())) {
                try {
                    llmReview = llmReviewService.reviewCode(result);
                    llmScore = llmReviewService.extractScore(llmReview);
                } catch (Exception e) {
                    log.warn("LLM评审失败: {}", e.getMessage());
                    llmReview = "LLM评审失败: " + e.getMessage();
                }
            }

            // 4. 保存评测结果
            saveEvaluationResult(task, result, llmReview, llmScore);

            // 5. 更新提交状态为"完成"
            updateSubmissionStatus(task.getSubmissionId(), 2);

            log.info("评测任务完成: taskId={}, score={}", task.getTaskId(), result.getTestScore());

        } catch (Exception e) {
            log.error("评测任务失败: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);
            // 更新提交状态为"失败"
            updateSubmissionStatus(task.getSubmissionId(), 3);
            throw e;
        }
    }

    /**
     * 更新提交状态
     */
    private void updateSubmissionStatus(Integer submissionId, Integer status) {
        Submission submission = new Submission();
        submission.setId(submissionId);
        submission.setStatus(status);
        submissionMapper.update(submission);
    }

    /**
     * 保存评测结果
     */
    private void saveEvaluationResult(CodeTask task, ExecutionResult result,
                                      String llmReview, Integer llmScore) {
        EvaluationResult evaluationResult = new EvaluationResult();
        evaluationResult.setTaskId(task.getSubmissionId());  // 使用submissionId作为taskId
        evaluationResult.setSubmissionId(task.getSubmissionId());
        evaluationResult.setStudentId(task.getStudentId());
        evaluationResult.setCompileStatus(result.getCompileStatus());
        evaluationResult.setTestPassed(result.getTestPassed());
        evaluationResult.setTestTotal(result.getTestTotal());
        evaluationResult.setTestScore(result.getTestScore());
        evaluationResult.setLlmScore(llmScore);
        evaluationResult.setTotalScore(result.getTestScore() + llmScore);
        evaluationResult.setLlmReview(llmReview);
        evaluationResult.setErrorMessage(result.getErrorMessage());
        evaluationResult.setCreatedAt(LocalDateTime.now());

        evaluationResultMapper.insert(evaluationResult);
    }
}