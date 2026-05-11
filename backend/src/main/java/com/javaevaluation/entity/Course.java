package com.javaevaluation.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体
 */
@Data
public class Course {
    private Integer id;
    private String name;
    private Integer teacherId;
    private Integer classId;
    private LocalDateTime createdAt;
}
