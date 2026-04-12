package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员实体
 */
@Data
public class Admin {
    private Integer id;
    private String username;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}