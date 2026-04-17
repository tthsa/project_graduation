package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Homework {
    private Integer id;
    private Integer courseId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}