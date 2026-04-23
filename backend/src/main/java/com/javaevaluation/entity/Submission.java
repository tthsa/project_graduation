package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交记录实体
 */
@Data
public class Submission {
    private Integer id;
    private Integer homeworkId;
    private Integer studentId;
    private LocalDateTime submitTime;
    private Integer status;  // 0=待评测, 1=评测中, 2=完成, 3=失败

    // 关联的文件列表
    private List<SubmissionFile> files;
}