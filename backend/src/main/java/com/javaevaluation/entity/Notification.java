package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
public class Notification {
    private Integer id;
    private Integer userId;
    private String userType;  // ADMIN, TEACHER, STUDENT
    private String title;
    private String content;
    private Integer isRead;  // 0=未读, 1=已读
    private LocalDateTime createdAt;
}