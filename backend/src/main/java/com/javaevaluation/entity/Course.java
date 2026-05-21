package com.javaevaluation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {
    private String name;
    private Integer teacherId;
    private Integer classId;
}
