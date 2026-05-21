package com.javaevaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试用例实体
 */
@Data
@TableName("test_case")
public class TestCase {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer homeworkId;
    private String name;
    private String input;
    private String expectedOutput;
    private Integer isPublic;
    private Integer sortOrder;

    @TableField("create_time")
    private LocalDateTime createdAt;
}
