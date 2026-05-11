package com.javaevaluation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Teacher {
    private Integer id;
    private String teacherNo;
    @JsonIgnore
    private String password;
    private String name;
    private String email;
    private String phone;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}