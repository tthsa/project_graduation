package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试用例实体
 */
@Data
public class TestCase {
    private Integer id;
    private Integer homeworkId;
    private String name;
    private String input;
    private String expectedOutput;
    private Integer score;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}