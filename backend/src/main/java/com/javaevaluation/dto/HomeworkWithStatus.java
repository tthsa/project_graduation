package com.javaevaluation.dto;

import com.javaevaluation.entity.Homework;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业带提交状态的DTO
 * 用于学生查看作业列表时显示提交状态
 */
@Data
public class HomeworkWithStatus {

    /**
     * 作业信息
     */
    private Homework homework;

    /**
     * 提交状态
     * 0=未提交, 1=待评测, 2=评测中, 3=已完成
     */
    private Integer submitStatus;

    /**
     * 提交ID
     */
    private Integer submissionId;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 测试分数
     */
    private Integer score;

    /**
     * 是否已截止
     */
    private Boolean expired;
}