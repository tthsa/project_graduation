package com.javaevaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提交历史实体
 */
@Data
@TableName("submission_history")
public class SubmissionHistory {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer submissionId;
    private LocalDateTime submitTime;
}
