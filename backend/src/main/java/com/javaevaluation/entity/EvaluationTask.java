package com.javaevaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评测任务实体
 */
@Data
@TableName("evaluation_task")
public class EvaluationTask {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer homeworkId;
    private String name;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
}
