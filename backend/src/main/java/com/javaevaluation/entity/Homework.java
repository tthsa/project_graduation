package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Homework {
    private Integer id;
    private Integer courseId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 测试得分权重 (0-100), 与 llmWeight 之和必须=100 */
    private Integer testWeight;
    /** LLM 评分权重 (0-100), 与 testWeight 之和必须=100 */
    private Integer llmWeight;
    /** A 等级阈值, final_score>=此值为 A */
    private Integer gradeAThreshold;
    /** B 等级阈值 */
    private Integer gradeBThreshold;
    /** C 等级阈值 (及格线) */
    private Integer gradeCThreshold;
    /** LLM 评分维度 JSON; null = 默认 [{name:"代码质量",weight:100}] */
    private String llmDimensions;
}