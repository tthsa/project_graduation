package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Admin {
    private Integer id;
    private String username;
    private String password;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}