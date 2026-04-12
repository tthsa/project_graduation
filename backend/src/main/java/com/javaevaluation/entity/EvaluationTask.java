package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评测任务实体
 */
@Data
public class EvaluationTask {
    private Integer id;
    private Integer homeworkId;
    private String status;  // PENDING, RUNNING, COMPLETED, FAILED
    private Integer totalCount;
    private Integer completedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
}