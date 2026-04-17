package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubmissionHistory {
    private Integer id;
    private Integer submissionId;
    private LocalDateTime submitTime;
}