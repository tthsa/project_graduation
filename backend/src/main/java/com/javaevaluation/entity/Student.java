package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Student {
    private Integer id;
    private String studentNo;
    private String password;
    private String name;
    private String email;
    private Integer classId;
    private Integer status;
    private Integer firstLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}