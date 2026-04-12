package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教师实体
 */
@Data
public class Teacher {
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private Integer status;  // 1=正常, 0=禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}