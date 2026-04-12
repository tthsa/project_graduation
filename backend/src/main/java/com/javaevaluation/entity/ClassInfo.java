package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 班级实体
 */
@Data
public class ClassInfo {
    private Integer id;
    private String name;
    private Integer teacherId;
    private String description;
    private Integer status;  // 1=正常, 0=禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}