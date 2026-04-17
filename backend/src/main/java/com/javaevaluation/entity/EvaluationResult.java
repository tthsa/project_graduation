package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvaluationResult {
    private Integer id;
    private Integer submissionId;
    private Integer testScore;
    private Integer llmScore;
    private String llmReview;
    private Long executionTime;
    private LocalDateTime createdAt;
}