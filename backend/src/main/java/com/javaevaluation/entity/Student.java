package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生实体
 */
@Data
public class Student {
    private Integer id;
    private String studentNo;
    private String password;
    private String name;
    private Integer classId;
    private String email;
    private Integer status;  // 1=正常, 0=禁用
    private Integer firstLogin;  // 1=首次登录, 0=已修改密码
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}