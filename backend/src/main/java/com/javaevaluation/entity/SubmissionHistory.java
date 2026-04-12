package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提交历史实体
 */
@Data
public class SubmissionHistory {
    private Integer id;
    private Integer submissionId;
    private String[] filePaths;
    private LocalDateTime submitTime;
}