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

    /** 作业标题（用于LLM题意判断） */
    private String homeworkTitle;

    /** 作业描述（用于LLM题意判断） */
    private String homeworkDescription;

    /** 时间戳 */
    private Long timestamp;
}