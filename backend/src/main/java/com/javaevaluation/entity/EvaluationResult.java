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

    /** 综合分 0-100, NULL=旧数据或还未评 */
    private Integer finalScore;
    /** 等级 A/B/C/D, NULL=同上 */
    private String grade;
    /** 各 LLM 维度分 JSON, NULL=单维度或 LLM 失败 */
    private String llmDimensionScores;
}