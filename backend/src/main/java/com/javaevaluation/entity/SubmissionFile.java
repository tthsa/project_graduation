package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提交文件实体
 */
@Data
public class SubmissionFile {
    private Integer id;
    private Integer submissionId;
    private String fileName;      // 文件名
    private String fileContent;   // 文件内容
    private Integer fileOrder;    // 文件顺序
    private LocalDateTime createdAt;
}