package com.javaevaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交记录实体
 */
@Data
@TableName("submission")
public class Submission {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer homeworkId;
    private Integer studentId;
    private LocalDateTime submitTime;
    private Integer status;  // 0=待评测, 1=评测中, 2=完成, 3=失败

    // 关联的文件列表（非数据库字段）
    @TableField(exist = false)
    private List<SubmissionFile> files;
}