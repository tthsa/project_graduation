package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提交文件实体
 */
@Data
public class SubmissionFile {
    private Integer id;
    private Integer submissionId;   // 关联提交记录
    private String filePath;        // 文件路径
    private String fileName;        // 文件名
    private Long fileSize;          // 文件大小（字节）
    private LocalDateTime uploadTime;  // 上传时间
}