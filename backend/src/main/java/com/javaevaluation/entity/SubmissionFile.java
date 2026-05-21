package com.javaevaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提交文件实体
 */
@Data
@TableName("submission_file")
public class SubmissionFile {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer submissionId;
    private String fileName;      // 文件名
    private String fileContent;   // 文件内容
    private Integer fileOrder;    // 文件顺序
    private LocalDateTime createdAt;
}