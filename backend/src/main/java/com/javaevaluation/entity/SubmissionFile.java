package com.javaevaluation.entity;

import lombok.Data;

@Data
public class SubmissionFile {
    private Integer id;
    private Integer submissionId;
    private String filePath;
}