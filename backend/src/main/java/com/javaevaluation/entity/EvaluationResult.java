package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评测结果实体
 */
@Data
public class EvaluationResult {
    private Integer id;
    private Integer taskId;
    private Integer submissionId;
    private Integer studentId;
    private String compileStatus;  // SUCCESS, COMPILE_ERROR, ERROR
    private Integer testPassed;
    private Integer testTotal;
    private Integer testScore;
    private Integer llmScore;
    private Integer totalScore;
    private String llmReview;
    private String errorMessage;
    private LocalDateTime createdAt;
}