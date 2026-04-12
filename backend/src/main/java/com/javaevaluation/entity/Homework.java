package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作业实体
 */
@Data
public class Homework {
    private Integer id;
    private String title;
    private String description;
    private Integer teacherId;
    private Integer classId;
    private LocalDateTime deadline;
    private Integer status;  // 1=正常, 0=禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}