package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提交记录实体
 */
@Data
public class Submission {
    private Integer id;
    private Integer homeworkId;
    private Integer studentId;
    private String[] filePaths;
    private LocalDateTime submitTime;
    private Integer status;  // 0=待评测, 1=评测中, 2=完成, 3=失败
}