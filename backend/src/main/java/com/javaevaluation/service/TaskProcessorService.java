package com.javaevaluation.service;

import com.javaevaluation.dto.CodeTask;
import com.javaevaluation.dto.DimensionScore;
import com.javaevaluation.dto.ExecutionResult;
import com.javaevaluation.dto.LlmDimension;
import com.javaevaluation.entity.EvaluationResult;
import com.javaevaluation.entity.Homework;
import com.javaevaluation.mapper.EvaluationResultMapper;
import com.javaevaluation.mapper.HomeworkMapper;
import com.javaevaluation.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    private final HomeworkMapper homeworkMapper;
    private final DockerSandboxService dockerSandboxService;
    private final LlmReviewService llmReviewService;
    private final ScoringService scoringService;
    private final ResultService resultService;

    @Transactional
    public void processTask(CodeTask task) {
        log.info("开始处理评测任务: taskId={}, submissionId={}", task.getTaskId(), task.getSubmissionId());

        try {
            updateSubmissionStatus(task.getSubmissionId(), 1);

            ExecutionResult result = dockerSandboxService.executeTask(task);

            // 加载作业配置 + 解析评分维度
            Homework homework = homeworkMapper.selectById(task.getHomeworkId());
            List<LlmDimension> dimensions = scoringService.parseDimensions(
                    homework == null ? null : homework.getLlmDimensions());

            // LLM 评审（传入作业标题和描述用于题意判断）
            String llmReview = null;
            Integer llmScore = null;
            String dimensionScoresJson = null;
            boolean isCompliant = true;
            if ("SUCCESS".equals(result.getCompileStatus())) {
                try {
                    llmReview = llmReviewService.reviewCode(task.getSubmissionId(), result, dimensions,
                            task.getHomeworkTitle(), task.getHomeworkDescription());

                    // 题意符合度检查：不符合题意的代码直接判定为 0 分
                    isCompliant = llmReviewService.isCompliant(llmReview);
                    if (!isCompliant) {
                        log.warn("题意不符合: submissionId={}, homeworkTitle={}",
                                task.getSubmissionId(), task.getHomeworkTitle());
                        llmScore = 0;
                        dimensionScoresJson = null;
                    } else {
                        List<DimensionScore> dimScores = llmReviewService.extractDimensionScores(llmReview, dimensions);
                        llmScore = scoringService.aggregateLlmScore(dimScores, dimensions);
                        dimensionScoresJson = scoringService.serializeDimensionScores(dimScores);
                        if (llmScore == null) {
                            // 多维度全部提取失败时, 退回到旧的单分数提取以兼容简单评审
                            llmScore = llmReviewService.extractScore(llmReview);
                        }
                    }
                } catch (Exception e) {
                    log.warn("LLM评审失败: {}", e.getMessage());
                    llmReview = "LLM评审失败: " + e.getMessage();
                }
            }

            Integer finalScore;
            String grade;
            if (!isCompliant) {
                // 题意不符合：综合分直接为 0，等级为 D
                finalScore = 0;
                grade = "D";
            } else {
                finalScore = scoringService.computeFinalScore(result.getTestScore(), llmScore, homework);
                grade = scoringService.computeGrade(finalScore, homework);
            }

            saveEvaluationResult(task, result, llmReview, llmScore, finalScore, grade, dimensionScoresJson);
            updateSubmissionStatus(task.getSubmissionId(), 2);

            log.info("评测任务完成: taskId={}, test={}, llm={}, final={}, grade={}, dims={}",
                    task.getTaskId(), result.getTestScore(), llmScore, finalScore, grade,
                    dimensionScoresJson == null ? "-" : "set");

        } catch (Exception e) {
            log.error("评测任务失败: taskId={}, error={}", task.getTaskId(), e.getMessage(), e);
            updateSubmissionStatus(task.getSubmissionId(), 3);
            throw e;
        }
    }

    public void updateSubmissionStatus(Integer submissionId, Integer status) {
        submissionMapper.updateStatus(submissionId, status);
    }

    private void saveEvaluationResult(CodeTask task, ExecutionResult result,
                                      String llmReview, Integer llmScore,
                                      Integer finalScore, String grade,
                                      String llmDimensionScoresJson) {
        EvaluationResult evaluationResult = new EvaluationResult();
        evaluationResult.setSubmissionId(task.getSubmissionId());
        evaluationResult.setTestScore(result.getTestScore());
        evaluationResult.setLlmScore(llmScore);
        evaluationResult.setLlmReview(llmReview);
        evaluationResult.setExecutionTime(0L);
        evaluationResult.setCreatedAt(LocalDateTime.now());
        evaluationResult.setFinalScore(finalScore);
        evaluationResult.setGrade(grade);
        evaluationResult.setLlmDimensionScores(llmDimensionScoresJson);

        evaluationResultMapper.insert(evaluationResult);
        resultService.evictCache(task.getSubmissionId());
    }
}
