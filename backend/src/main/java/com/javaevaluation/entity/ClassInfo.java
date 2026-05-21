package com.javaevaluation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class_info")
public class ClassInfo extends BaseEntity {
    private String name;
    private Integer teacherId;
    private String description;
    private Integer status;  // 1=正常, 0=禁用
}