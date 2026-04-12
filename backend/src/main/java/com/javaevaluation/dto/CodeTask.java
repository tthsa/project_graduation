package com.javaevaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 评测任务DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeTask implements Serializable {

    /** 任务ID */
    private String taskId;

    /** 提交ID */
    private Integer submissionId;

    /** 学生ID */
    private Integer studentId;

    /** 作业ID */
    private Integer homeworkId;

    /** 文件路径列表 */
    private String[] filePaths;

    /** 时间戳 */
    private Long timestamp;
}