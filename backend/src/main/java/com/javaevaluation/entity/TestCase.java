package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCase {
    private Integer id;
    private Integer homeworkId;
    private String name;
    private String input;
    private String expectedOutput;
    private Integer isPublic;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}