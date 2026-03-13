package com.codereview.backend.dto;

import lombok.Builder;
import lombok.Data;
@Data
@Builder

public class ExecutionResult {
    /**任务 ID*/
    private String taskId;

    /**是否成功*/
    private Boolean success;

    /**标准输出*/
    private String output;

    /**错误输出*/
    private String error;

    /** 退出码*/
    private Integer exitCode;

    /**执行时间（毫秒）*/
    private Long executionTime;

    /**LLM 评审结果*/
    private String llmReview;

    /**完成时间戳*/
    private Long timestamp;

    /**消息*/
    private String message;

}
