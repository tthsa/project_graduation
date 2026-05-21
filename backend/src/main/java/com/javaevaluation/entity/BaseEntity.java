package com.javaevaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类，包含公共字段
 */
@Data
public abstract class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("create_time")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
