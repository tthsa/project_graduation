package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
}